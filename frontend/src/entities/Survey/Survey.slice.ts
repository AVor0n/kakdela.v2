import type { AnswerOption, Question, QuestionType } from '@/shared/types/Question.type';
import type { Page, Survey } from '@/shared/types/Survey.type';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export interface ISurveyState {
    surveys: Survey[];
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

const surveySlice = createSlice({
    name: 'survey',
    initialState,
    reducers: {
        setSurveys: (state, action: PayloadAction<{ surveys: Survey[] }>) => {
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
            state.selectedSurvey.pages.push(page);
        },
        setSelectedQuestion: (state, action: PayloadAction<{ question: Question; pageIndex: number }>) => {
            const { question, pageIndex } = action.payload;
            state.selectedQuestion = question;
            state.currentQuestionPageIndex = pageIndex;
        },
        updateQuestionTitle: (state, action: PayloadAction<{ id: string; title: string }>) => {
            if (!state.selectedSurvey) return;
            const { id, title } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.map((question) => {
                if (question.id === id) {
                    question.title = title;
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
        addQuestionOptions: (state, action: PayloadAction<{ answerOption: AnswerOption }>) => {
            if (!state.selectedSurvey) return;
            const { answerOption } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.answerOptions?.push(answerOption);
                }
            });
        },
        deleteOption: (state, action: PayloadAction<{ id: string }>) => {
            if (!state.selectedSurvey) return;
            const { id } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.answerOptions = question.answerOptions?.filter((option) => option.id !== id);
                }
            });
        },
        addQuestion: (state, action: PayloadAction<{ question: Question; pageIndex?: number }>) => {
            if (!state.selectedSurvey) return;
            const { question, pageIndex } = action.payload;
            if (state.selectedSurvey.pages.length === 1) {
                state.selectedSurvey.pages[0].questions.push(question);
                return;
            }

            if (pageIndex !== undefined) {
                state.selectedSurvey.pages[pageIndex].questions.push(question);
                return;
            }
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.push(question);
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
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions = state.selectedSurvey.pages[
                state.currentQuestionPageIndex
            ].questions.filter((question) => question.id !== id);
        },
        duplicateQuestion: (state, action: PayloadAction<{ afterQuestionId: string; question: Question }>) => {
            if (!state.selectedSurvey) return;
            const { afterQuestionId, question } = action.payload;
            const questions = state.selectedSurvey.pages[state.currentQuestionPageIndex].questions;
            const index = questions.findIndex((q) => q.id === afterQuestionId);
            if (index !== -1) {
                questions.splice(index + 1, 0, question);
            }
        },
        deletePage: (state, action: PayloadAction<{ pageId: string }>) => {
            if (!state.selectedSurvey) return;
            const { pageId } = action.payload;
            state.selectedSurvey.pages = state.selectedSurvey.pages.filter((page) => page.id !== pageId);
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
    },
});

export const {
    setSurveys,
    setSelectedSurvey,
    updateQuestionTitle,
    updateQuestionType,
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
} = surveySlice.actions;
export default surveySlice.reducer;
