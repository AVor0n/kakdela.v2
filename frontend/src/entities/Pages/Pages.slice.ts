import type { AnswerOption, AnswerOptionOrder, Question, QuestionType } from '@/shared/types/Question.type';
import type { Page } from '@/shared/types/Survey.type';
import type { Condition } from '@/shared/types/Condition.type';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface PageState {
    pages: Page[];
    selectedQuestion: Question | null;
    currentQuestionPageIndex: number;
}

const initialState: PageState = {
    pages: [],
    selectedQuestion: null,
    currentQuestionPageIndex: 0,
};

function normalizeSerialNumbers<T extends { serialNumber: number }>(items: T[]) {
    items.forEach((item, index) => {
        item.serialNumber = index + 1;
    });
}

function getInsertIndex(serialNumber: number, length: number) {
    return Math.min(Math.max(serialNumber - 1, 0), length);
}

const pagesSlice = createSlice({
    name: 'pages',
    initialState,
    reducers: {
        setPages: (state, action: PayloadAction<{ pages: Page[] }>) => {
            const { pages } = action.payload;
            state.pages = pages;
        },
        setSelectedPage: (state, action: PayloadAction<{ pageIndex: number }>) => {
            const { pageIndex } = action.payload;
            if (pageIndex >= 0 && pageIndex < state.pages.length) {
                state.currentQuestionPageIndex = pageIndex;
            }
        },
        addPage: (state, action: PayloadAction<{ page: Page }>) => {
            const { page } = action.payload;
            const pages = state.pages;
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
            const { id, text } = action.payload;
            state.pages[state.currentQuestionPageIndex].questions.map((question) => {
                if (question.id === id) {
                    question.text = text;
                }
            });
        },
        updateQuestionDescription: (state, action: PayloadAction<{ id: string; description: string | null }>) => {
            const { id, description } = action.payload;
            state.pages[state.currentQuestionPageIndex].questions.map((question) => {
                if (question.id === id) {
                    question.description = description;
                }
            });
        },

        updateQuestionType: (state, action: PayloadAction<{ id: string; type: QuestionType }>) => {
            const { id, type } = action.payload;
            state.pages[state.currentQuestionPageIndex].questions.map((question) => {
                if (question.id === id) {
                    question.type = type;
                }
            });
        },
        updateAnswerOptionOrder: (
            state,
            action: PayloadAction<{ id: string; answerOptionOrder: AnswerOptionOrder }>,
        ) => {
            const { id, answerOptionOrder } = action.payload;
            const question = state.pages
                .flatMap((page) => page.questions)
                .find((pageQuestion) => pageQuestion.id === id);

            if (question?.type === 'SINGLE_CHOICE' || question?.type === 'MULTIPLE_CHOICE') {
                question.answerOptionOrder = answerOptionOrder;
            }
        },
        addQuestionOptions: (state, action: PayloadAction<{ answerOption: AnswerOption }>) => {
            const { answerOption } = action.payload;
            if (!state.selectedQuestion) return;

            const question = state.pages
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
            const { id } = action.payload;
            if (!state.selectedQuestion) return;

            const question = state.pages
                .flatMap((page) => page.questions)
                .find((pageQuestion) => pageQuestion.id === state.selectedQuestion?.id);
            if (!question?.answerOptions) return;

            question.answerOptions = question.answerOptions.filter((option) => option.id !== id);
            normalizeSerialNumbers(question.answerOptions);
            state.selectedQuestion = question;
        },
        addQuestion: (state, action: PayloadAction<{ question: Question; pageIndex?: number }>) => {
            const { question, pageIndex } = action.payload;
            const targetPageIndex = pageIndex ?? state.currentQuestionPageIndex;
            const page = state.pages[targetPageIndex];
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
            const { value } = action.payload;
            state.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.isMandatory = value;
                }
            });
        },
        setOptionValue: (state, action: PayloadAction<{ answerOption: AnswerOption }>) => {
            const { answerOption } = action.payload;
            state.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
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
            const { id } = action.payload;
            const page = state.pages[state.currentQuestionPageIndex];
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
            const { afterQuestionId, question } = action.payload;
            const questions = state.pages[state.currentQuestionPageIndex].questions;
            const index = questions.findIndex((q) => q.id === afterQuestionId);
            if (index !== -1) {
                questions.splice(index + 1, 0, question);
                normalizeSerialNumbers(questions);
            }
        },
        deletePage: (state, action: PayloadAction<{ pageId: string }>) => {
            const { pageId } = action.payload;
            const selectedQuestionId = state.selectedQuestion?.id;
            const deletedPageIndex = state.pages.findIndex((page) => page.id === pageId);
            const deletedPage = state.pages.find((page) => page.id === pageId);
            const deletedSelectedQuestion =
                Boolean(selectedQuestionId) &&
                Boolean(deletedPage?.questions.some((question) => question.id === selectedQuestionId));

            state.pages = state.pages.filter((page) => page.id !== pageId);
            normalizeSerialNumbers(state.pages);

            if (deletedSelectedQuestion) {
                state.selectedQuestion = null;
                state.currentQuestionPageIndex = Math.min(
                    state.currentQuestionPageIndex,
                    Math.max(state.pages.length - 1, 0),
                );
                return;
            }

            if (selectedQuestionId) {
                const selectedPageIndex = state.pages.findIndex((page) =>
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
                Math.max(state.pages.length - 1, 0),
            );
        },
        reorderPages: (
            state,
            action: PayloadAction<{
                activePageId: string;
                overPageId: string;
            }>,
        ) => {
            const { activePageId, overPageId } = action.payload;
            if (activePageId === overPageId) return;

            const activeIndex = state.pages.findIndex((page) => page.id === activePageId);
            const overIndex = state.pages.findIndex((page) => page.id === overPageId);
            if (activeIndex === -1 || overIndex === -1) return;

            const [movedPage] = state.pages.splice(activeIndex, 1);
            state.pages.splice(overIndex, 0, movedPage);
            state.pages.forEach((page, index) => {
                page.serialNumber = index + 1;
            });

            if (state.selectedQuestion) {
                const selectedQuestionId = state.selectedQuestion.id;
                const selectedPageIndex = state.pages.findIndex((page) =>
                    page.questions.some((question) => question.id === selectedQuestionId),
                );
                if (selectedPageIndex !== -1) {
                    state.currentQuestionPageIndex = selectedPageIndex;
                }
            }
        },
        setSurveyPages: (state, action: PayloadAction<{ pages: Page[] }>) => {
            const { pages } = action.payload;

            state.pages = pages;

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
            state.pages.map((page) => {
                page.questions.map((q) => {
                    if (q.id === question.id) {
                        Object.assign(q, question);
                    }
                });
            });
        },
        setPage: (state, action: PayloadAction<{ page: Page }>) => {
            const { page } = action.payload;
            state.pages.map((p) => {
                if (p.id === page.id) {
                    Object.assign(p, page);
                }
            });
        },
        reorderQuestions: (
            state,
            action: PayloadAction<{
                pageIndex: number;
                activeQuestionId: string;
                overQuestionId: string;
            }>,
        ) => {
            const { pageIndex, activeQuestionId, overQuestionId } = action.payload;
            if (activeQuestionId === overQuestionId) return;

            const questions = state.pages[pageIndex]?.questions;
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
            const { pageIndex, questions } = action.payload;
            const page = state.pages[pageIndex];
            if (!page) return;

            page.questions = questions;

            if (state.selectedQuestion) {
                const selectedQuestionId = state.selectedQuestion.id;
                state.selectedQuestion =
                    questions.find((question) => question.id === selectedQuestionId) ?? state.selectedQuestion;
            }
        },
        upsertPageCondition: (state, action: PayloadAction<{ pageId: string; condition: Condition }>) => {
            const page = state.pages.find(({ id }) => id === action.payload.pageId);
            if (!page) return;
            const conditionIndex = page.conditions.findIndex(({ id }) => id === action.payload.condition.id);
            if (conditionIndex >= 0) page.conditions[conditionIndex] = action.payload.condition;
            else page.conditions.push(action.payload.condition);
        },
        deletePageCondition: (state, action: PayloadAction<{ pageId: string; conditionId: string }>) => {
            const page = state.pages.find(({ id }) => id === action.payload.pageId);
            if (page) page.conditions = page.conditions.filter(({ id }) => id !== action.payload.conditionId);
        },
        reorderAnswerOptions: (
            state,
            action: PayloadAction<{
                questionId: string;
                activeOptionId: string;
                overOptionId: string;
            }>,
        ) => {
            const { questionId, activeOptionId, overOptionId } = action.payload;
            if (activeOptionId === overOptionId) return;

            const question = state.pages
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
            const { questionId, answerOptions } = action.payload;

            const question = state.pages
                .flatMap((page) => page.questions)
                .find((pageQuestion) => pageQuestion.id === questionId);
            if (!question?.answerOptions) return;

            question.answerOptions = answerOptions;

            if (state.selectedQuestion?.id === questionId) {
                state.selectedQuestion = question;
            }
        },
    },
});

export const {
    setPages,
    setSelectedPage,
    addPage,
    setSelectedQuestion,
    updateQuestionText,
    updateQuestionDescription,
    updateQuestionType,
    updateAnswerOptionOrder,
    addQuestionOptions,
    deleteOption,
    addQuestion,
    setOptionValue,
    deleteQuestion,
    duplicateQuestion,
    deletePage,
    reorderPages,
    setSurveyPages,
    setQuestion,
    setPage,
    reorderQuestions,
    setPageQuestions,
    upsertPageCondition,
    deletePageCondition,
    reorderAnswerOptions,
    setQuestionAnswerOptions,
    setMandatory,
} = pagesSlice.actions;
export default pagesSlice.reducer;
