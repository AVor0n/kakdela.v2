import type { AnswerOption, AnswerOptionOrder, Question, QuestionType } from '@/shared/types/Question.type';
import type { ClosingPage, Page, Survey, SurveyListItem } from '@/shared/types/Survey.type';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export interface ISurveyState {
    surveys: SurveyListItem[];
    currentQuestionPageIndex: number;
    selectedQuestion: Question | null;
    selectedSurvey: Survey | null;
}

const initialState: ISurveyState = {
    surveys: [],
    currentQuestionPageIndex: 0,
    selectedQuestion: null,
    selectedSurvey: null,
};

function normalizeSerialNumbers<T extends { serialNumber: number }>(items: T[]) {
    items.forEach((item, index) => {
        item.serialNumber = index + 1;
    });
}

function getInsertIndex(serialNumber: number, length: number) {
    return Math.min(Math.max(serialNumber - 1, 0), length);
}

const surveySlice = createSlice({
    name: 'survey',
    initialState,
    reducers: {
        setSurveys: (state, action: PayloadAction<{ surveys: SurveyListItem[] }>) => {
            const { surveys } = action.payload;
            state.surveys = surveys;
        },
        setSelectedSurvey: (state, action: PayloadAction<{ survey: Survey | null }>) => {
            const { survey } = action.payload;
            state.selectedSurvey = survey;
        },
        addPage: (state, action: PayloadAction<{ page: Page }>) => {
            if (!state.selectedSurvey) return;
            const { page } = action.payload;
            const pages = state.selectedSurvey.pages;
            const previousPagesLength = pages.length;
            const insertIndex = getInsertIndex(page.serialNumber, pages.length);

            pages.splice(insertIndex, 0, page);
            normalizeSerialNumbers(pages);

            if (state.selectedQuestion) {
                const selectedQuestionId = state.selectedQuestion.id;
                const selectedPageIndex = pages.findIndex((surveyPage) =>
                    surveyPage.questions.some((question) => question.id === selectedQuestionId),
                );
                if (selectedPageIndex !== -1) {
                    state.currentQuestionPageIndex = selectedPageIndex;
                }
                return;
            }

            if (previousPagesLength > 0 && insertIndex <= state.currentQuestionPageIndex) {
                state.currentQuestionPageIndex += 1;
            }
        },
        setSelectedQuestion: (state, action: PayloadAction<{ question: Question; pageIndex: number }>) => {
            const { question, pageIndex } = action.payload;
            state.selectedQuestion = question;
            state.currentQuestionPageIndex = pageIndex;
        },
        updateQuestionText: (state, action: PayloadAction<{ id: string; text: string }>) => {
            if (!state.selectedSurvey) return;
            const { id, text } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.map((question) => {
                if (question.id === id) {
                    question.text = text;
                }
            });
        },
        updateQuestionDescription: (state, action: PayloadAction<{ id: string; description: string | null }>) => {
            if (!state.selectedSurvey) return;
            const { id, description } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.map((question) => {
                if (question.id === id) {
                    question.description = description;
                }
            });
        },

        updateQuestionType: (state, action: PayloadAction<{ id: string; type: QuestionType }>) => {
            if (state.selectedSurvey === null) return;
            const { id, type } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.map((question) => {
                if (question.id === id) {
                    question.type = type;
                }
            });
        },
        updateAnswerOptionOrder: (
            state,
            action: PayloadAction<{ id: string; answerOptionOrder: AnswerOptionOrder }>,
        ) => {
            if (state.selectedSurvey === null) return;
            const { id, answerOptionOrder } = action.payload;
            const question = state.selectedSurvey.pages
                .flatMap((page) => page.questions)
                .find((pageQuestion) => pageQuestion.id === id);

            if (question?.type === 'SINGLE_CHOICE' || question?.type === 'MULTIPLE_CHOICE') {
                question.answerOptionOrder = answerOptionOrder;
            }
        },
        addQuestionOptions: (state, action: PayloadAction<{ answerOption: AnswerOption }>) => {
            if (!state.selectedSurvey) return;
            const { answerOption } = action.payload;
            if (!state.selectedQuestion) return;

            const question = state.selectedSurvey.pages
                .flatMap((page) => page.questions)
                .find((pageQuestion) => pageQuestion.id === state.selectedQuestion?.id);
            if (!question?.answerOptions) return;

            question.answerOptions.splice(
                getInsertIndex(answerOption.serialNumber, question.answerOptions.length),
                0,
                answerOption,
            );
            normalizeSerialNumbers(question.answerOptions);
            state.selectedQuestion = question;
        },
        deleteOption: (state, action: PayloadAction<{ id: string }>) => {
            if (!state.selectedSurvey) return;
            const { id } = action.payload;
            if (!state.selectedQuestion) return;

            const question = state.selectedSurvey.pages
                .flatMap((page) => page.questions)
                .find((pageQuestion) => pageQuestion.id === state.selectedQuestion?.id);
            if (!question?.answerOptions) return;

            question.answerOptions = question.answerOptions.filter((option) => option.id !== id);
            normalizeSerialNumbers(question.answerOptions);
            state.selectedQuestion = question;
        },
        addQuestion: (state, action: PayloadAction<{ question: Question; pageIndex?: number }>) => {
            if (!state.selectedSurvey) return;
            const { question, pageIndex } = action.payload;
            const targetPageIndex = pageIndex ?? state.currentQuestionPageIndex;
            const page = state.selectedSurvey.pages[targetPageIndex];
            if (!page) return;

            page.questions.splice(getInsertIndex(question.serialNumber, page.questions.length), 0, question);
            normalizeSerialNumbers(page.questions);

            if (state.selectedQuestion) {
                const selectedQuestionId = state.selectedQuestion.id;
                state.selectedQuestion =
                    page.questions.find((pageQuestion) => pageQuestion.id === selectedQuestionId) ??
                    state.selectedQuestion;
            }
        },
        setMandatory: (state, action: PayloadAction<{ value: boolean }>) => {
            if (!state.selectedSurvey) return;
            const { value } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.isMandatory = value;
                }
            });
        },
        setOptionValue: (state, action: PayloadAction<{ answerOption: AnswerOption }>) => {
            if (!state.selectedSurvey) return;
            const { answerOption } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    if (question.answerOptions) {
                        question.answerOptions = question.answerOptions.map((option) => {
                            if (option.id === answerOption.id) {
                                return answerOption;
                            }
                            return option;
                        });
                    }
                }
            });
        },
        deleteQuestion: (state, action: PayloadAction<{ id: string }>) => {
            if (!state.selectedSurvey) return;
            const { id } = action.payload;
            const page = state.selectedSurvey.pages[state.currentQuestionPageIndex];
            if (!page) return;

            page.questions = page.questions.filter((question) => question.id !== id);
            normalizeSerialNumbers(page.questions);

            if (state.selectedQuestion?.id === id) {
                state.selectedQuestion = null;
                return;
            }

            if (state.selectedQuestion) {
                const selectedQuestionId = state.selectedQuestion.id;
                state.selectedQuestion =
                    page.questions.find((question) => question.id === selectedQuestionId) ?? state.selectedQuestion;
            }
        },
        duplicateQuestion: (state, action: PayloadAction<{ afterQuestionId: string; question: Question }>) => {
            if (!state.selectedSurvey) return;
            const { afterQuestionId, question } = action.payload;
            const questions = state.selectedSurvey.pages[state.currentQuestionPageIndex].questions;
            const index = questions.findIndex((q) => q.id === afterQuestionId);
            if (index !== -1) {
                questions.splice(index + 1, 0, question);
                normalizeSerialNumbers(questions);
            }
        },
        deletePage: (state, action: PayloadAction<{ pageId: string }>) => {
            if (!state.selectedSurvey) return;
            const { pageId } = action.payload;
            const selectedQuestionId = state.selectedQuestion?.id;
            const deletedPageIndex = state.selectedSurvey.pages.findIndex((page) => page.id === pageId);
            const deletedPage = state.selectedSurvey.pages.find((page) => page.id === pageId);
            const deletedSelectedQuestion =
                Boolean(selectedQuestionId) &&
                Boolean(deletedPage?.questions.some((question) => question.id === selectedQuestionId));

            state.selectedSurvey.pages = state.selectedSurvey.pages.filter((page) => page.id !== pageId);
            normalizeSerialNumbers(state.selectedSurvey.pages);

            if (deletedSelectedQuestion) {
                state.selectedQuestion = null;
                state.currentQuestionPageIndex = Math.min(
                    state.currentQuestionPageIndex,
                    Math.max(state.selectedSurvey.pages.length - 1, 0),
                );
                return;
            }

            if (selectedQuestionId) {
                const selectedPageIndex = state.selectedSurvey.pages.findIndex((page) =>
                    page.questions.some((question) => question.id === selectedQuestionId),
                );
                if (selectedPageIndex !== -1) {
                    state.currentQuestionPageIndex = selectedPageIndex;
                    return;
                }
            }

            if (deletedPageIndex !== -1 && deletedPageIndex < state.currentQuestionPageIndex) {
                state.currentQuestionPageIndex -= 1;
            }

            state.currentQuestionPageIndex = Math.min(
                state.currentQuestionPageIndex,
                Math.max(state.selectedSurvey.pages.length - 1, 0),
            );
        },
        reorderPages: (
            state,
            action: PayloadAction<{
                activePageId: string;
                overPageId: string;
            }>,
        ) => {
            if (!state.selectedSurvey) return;
            const { activePageId, overPageId } = action.payload;
            if (activePageId === overPageId) return;

            const pages = state.selectedSurvey.pages;
            const activeIndex = pages.findIndex((page) => page.id === activePageId);
            const overIndex = pages.findIndex((page) => page.id === overPageId);
            if (activeIndex === -1 || overIndex === -1) return;

            const [movedPage] = pages.splice(activeIndex, 1);
            pages.splice(overIndex, 0, movedPage);
            pages.forEach((page, index) => {
                page.serialNumber = index + 1;
            });

            if (state.selectedQuestion) {
                const selectedQuestionId = state.selectedQuestion.id;
                const selectedPageIndex = pages.findIndex((page) =>
                    page.questions.some((question) => question.id === selectedQuestionId),
                );
                if (selectedPageIndex !== -1) {
                    state.currentQuestionPageIndex = selectedPageIndex;
                }
            }
        },
        setSurveyPages: (state, action: PayloadAction<{ pages: Page[] }>) => {
            if (!state.selectedSurvey) return;
            const { pages } = action.payload;

            state.selectedSurvey.pages = pages;

            if (state.selectedQuestion) {
                const selectedQuestionId = state.selectedQuestion.id;
                const selectedPageIndex = pages.findIndex((page) =>
                    page.questions.some((question) => question.id === selectedQuestionId),
                );

                if (selectedPageIndex !== -1) {
                    state.currentQuestionPageIndex = selectedPageIndex;
                    state.selectedQuestion =
                        pages[selectedPageIndex].questions.find((question) => question.id === selectedQuestionId) ??
                        state.selectedQuestion;
                }
            }
        },
        setQuestion: (state, action: PayloadAction<{ question: Question }>) => {
            const { question } = action.payload;
            state.selectedSurvey?.pages.map((page) => {
                page.questions.map((q) => {
                    if (q.id === question.id) {
                        Object.assign(q, question);
                    }
                });
            });
        },
        setPage: (state, action: PayloadAction<{ page: Page }>) => {
            const { page } = action.payload;
            state.selectedSurvey?.pages.map((p) => {
                if (p.id === page.id) {
                    Object.assign(p, page);
                }
            });
        },
        setClosingPage: (state, action: PayloadAction<{ closingPage: ClosingPage | null }>) => {
            if (!state.selectedSurvey) return;
            state.selectedSurvey.closingPage = action.payload.closingPage;
        },
        patchClosingPage: (state, action: PayloadAction<Partial<ClosingPage>>) => {
            if (!state.selectedSurvey?.closingPage) return;
            Object.assign(state.selectedSurvey.closingPage, action.payload);
        },
        reorderQuestions: (
            state,
            action: PayloadAction<{
                pageIndex: number;
                activeQuestionId: string;
                overQuestionId: string;
            }>,
        ) => {
            if (!state.selectedSurvey) return;
            const { pageIndex, activeQuestionId, overQuestionId } = action.payload;
            if (activeQuestionId === overQuestionId) return;

            const questions = state.selectedSurvey.pages[pageIndex]?.questions;
            if (!questions) return;

            const activeIndex = questions.findIndex((question) => question.id === activeQuestionId);
            const overIndex = questions.findIndex((question) => question.id === overQuestionId);
            if (activeIndex === -1 || overIndex === -1) return;

            const [movedQuestion] = questions.splice(activeIndex, 1);
            questions.splice(overIndex, 0, movedQuestion);
            questions.forEach((question, index) => {
                question.serialNumber = index + 1;
            });

            if (state.selectedQuestion) {
                const selectedQuestionId = state.selectedQuestion.id;
                state.selectedQuestion =
                    questions.find((question) => question.id === selectedQuestionId) ?? state.selectedQuestion;
            }
        },
        setPageQuestions: (state, action: PayloadAction<{ pageIndex: number; questions: Question[] }>) => {
            if (!state.selectedSurvey) return;
            const { pageIndex, questions } = action.payload;
            const page = state.selectedSurvey.pages[pageIndex];
            if (!page) return;

            page.questions = questions;

            if (state.selectedQuestion) {
                const selectedQuestionId = state.selectedQuestion.id;
                state.selectedQuestion =
                    questions.find((question) => question.id === selectedQuestionId) ?? state.selectedQuestion;
            }
        },
        reorderAnswerOptions: (
            state,
            action: PayloadAction<{
                questionId: string;
                activeOptionId: string;
                overOptionId: string;
            }>,
        ) => {
            if (!state.selectedSurvey) return;
            const { questionId, activeOptionId, overOptionId } = action.payload;
            if (activeOptionId === overOptionId) return;

            const question = state.selectedSurvey.pages
                .flatMap((page) => page.questions)
                .find((pageQuestion) => pageQuestion.id === questionId);
            if (!question?.answerOptions) return;

            const activeIndex = question.answerOptions.findIndex((option) => option.id === activeOptionId);
            const overIndex = question.answerOptions.findIndex((option) => option.id === overOptionId);
            if (activeIndex === -1 || overIndex === -1) return;

            const [movedOption] = question.answerOptions.splice(activeIndex, 1);
            question.answerOptions.splice(overIndex, 0, movedOption);
            question.answerOptions.forEach((option, index) => {
                option.serialNumber = index + 1;
            });

            if (state.selectedQuestion?.id === questionId) {
                state.selectedQuestion = question;
            }
        },
        setQuestionAnswerOptions: (
            state,
            action: PayloadAction<{ questionId: string; answerOptions: AnswerOption[] }>,
        ) => {
            if (!state.selectedSurvey) return;
            const { questionId, answerOptions } = action.payload;

            const question = state.selectedSurvey.pages
                .flatMap((page) => page.questions)
                .find((pageQuestion) => pageQuestion.id === questionId);
            if (!question?.answerOptions) return;

            question.answerOptions = answerOptions;

            if (state.selectedQuestion?.id === questionId) {
                state.selectedQuestion = question;
            }
        },
        deleteSurvey: (state, action: PayloadAction<{ surveyId: string }>) => {
            const { surveyId } = action.payload;
            state.surveys = state.surveys.filter((survey) => survey.id !== surveyId);
            if (state.selectedSurvey?.id === surveyId) {
                state.selectedSurvey = null;
                state.selectedQuestion = null;
                state.currentQuestionPageIndex = 0;
            }
        },
        addSurvey: (state, action: PayloadAction<{ survey: Survey }>) => {
            const { survey } = action.payload;
            const newSurveyListItem: SurveyListItem = {
                id: survey.id,
                title: survey.title,
                description: survey.description,
                createdAt: survey.createdAt,
                isPublished: survey.isPublished,
                userRole: 'AUTHOR', // Assuming the user creating the survey is the author
            };
            state.surveys.push(newSurveyListItem);
        },
    },
});

export const {
    setSurveys,
    setSelectedSurvey,
    updateQuestionText,
    updateQuestionType,
    updateAnswerOptionOrder,
    updateQuestionDescription,
    addQuestionOptions,
    setSelectedQuestion,
    deleteOption,
    addQuestion,
    setMandatory,
    setOptionValue,
    deleteQuestion,
    duplicateQuestion,
    addPage,
    deletePage,
    setQuestion,
    setPage,
    setClosingPage,
    patchClosingPage,
    reorderPages,
    setSurveyPages,
    reorderQuestions,
    setPageQuestions,
    reorderAnswerOptions,
    setQuestionAnswerOptions,
    deleteSurvey,
    addSurvey,
} = surveySlice.actions;
export default surveySlice.reducer;
