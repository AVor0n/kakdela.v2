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
        setSelectedSurvey: (state, action: PayloadAction<{ survey: Survey }>) => {
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
        duplicateQuestion: (state, action: PayloadAction<{ id: string }>) => {
            if (!state.selectedSurvey) return;
            const { id } = action.payload;
            const questionToDuplicate = state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.find(
                (question) => question.id === id,
            );
            const index = state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.findIndex(
                (question) => question.id === id,
            );
            if (questionToDuplicate) {
                const duplicatedQuestion = {
                    ...questionToDuplicate,
                    id: (state.selectedSurvey.pages[0].questions.length + 1).toString(),
                    serialNumber: state.selectedSurvey.pages[0].questions.length + 1,
                };
                state.selectedSurvey.pages[0].questions.splice(index + 1, 0, duplicatedQuestion);
            }
        },
        deletePage: (state, action: PayloadAction<{ pageId: string }>) => {
            if (!state.selectedSurvey) return;
            const { pageId } = action.payload;
            state.selectedSurvey.pages = state.selectedSurvey.pages.filter((page) => page.id !== pageId);
        },
    },
});

export const {
    setSurveys,
    setSelectedSurvey,
    updateQuestionTitle,
    updateQuestionType,
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
} = surveySlice.actions;
export default surveySlice.reducer;
