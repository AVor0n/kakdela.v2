import type { Question, QuestionType } from '@/shared/types/Question.type';
import type { Survey } from '@/shared/types/Survey.type';
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
        addPage: (state) => {
            if (!state.selectedSurvey) return;
            const newPage = {
                id: (state.selectedSurvey.pages.length + 1).toString(),
                title: `Страница ${state.selectedSurvey.pages.length + 1}`,
                description: null,
                surveyId: state.selectedSurvey.id,
                serialNumber: state.selectedSurvey.pages.length + 1,
                questions: [],
            };
            const newQuestion = {
                answerOptionOrder: null,
                answerOptions: ['Вариант 1'],
                condition: null,
                description: null,
                id: (state.selectedSurvey.pages[0].questions.length + 1).toString(),
                mandatory: false,
                serialNumber: state.selectedSurvey.pages[0].questions.length + 1,
                title: '',
                type: 'SINGLE_CHOICE',
                visible: true,
            } satisfies Question;
            state.selectedSurvey.pages.push(newPage);
            state.selectedSurvey.pages[state.selectedSurvey.pages.length - 1].questions.push(newQuestion);
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

        updateQuestionType: (state, action: PayloadAction<{ type: QuestionType }>) => {
            if (state.selectedSurvey === null) return;
            const { type } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.map((question) => {
                if (state.selectedQuestion) {
                    if (question.id === state.selectedQuestion.id) {
                        question.type = type;
                    }
                }
            });
        },
        addQuestionOptions: (state) => {
            if (!state.selectedSurvey) return;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.answerOptions?.push(`Вариант ${question.answerOptions.length + 1}`);
                }
            });
        },
        addAnotherOption: (state) => {
            if (!state.selectedSurvey) return;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.answerOptions?.push('Другое');
                }
            });
        },
        deleteOption: (state, action: PayloadAction<{ removeIndex: number }>) => {
            if (!state.selectedSurvey) return;
            const { removeIndex } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.answerOptions = question.answerOptions?.filter((_, index) => index !== removeIndex);
                }
            });
        },
        addQuestion: (state) => {
            if (!state.selectedSurvey) return;
            if (state.selectedSurvey.pages.length === 0) {
                const newPage = {
                    id: (state.selectedSurvey.pages.length + 1).toString(),
                    title: `Страница ${state.selectedSurvey.pages.length + 1}`,
                    description: null,
                    surveyId: state.selectedSurvey.id,
                    serialNumber: state.selectedSurvey.pages.length + 1,
                    questions: [],
                };
                state.selectedSurvey.pages.push(newPage);
            }
            const newQuestion = {
                answerOptionOrder: null,
                answerOptions: ['Вариант 1'],
                condition: null,
                description: null,
                id: (state.selectedSurvey.pages[0].questions.length + 1).toString(),
                mandatory: false,
                serialNumber: state.selectedSurvey.pages[0].questions.length + 1,
                title: '',
                type: 'SINGLE_CHOICE',
                visible: true,
            } satisfies Question;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.push(newQuestion);
        },
        setMandatory: (state) => {
            if (!state.selectedSurvey) return;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.mandatory = !question.mandatory;
                }
            });
        },
        setOptionValue: (state, action: PayloadAction<{ value: string; index: number }>) => {
            if (!state.selectedSurvey) return;
            const { value, index } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    if (question.answerOptions) {
                        question.answerOptions[index] = value;
                    }
                }
            });
        },
        deleteQuestion: (state, action: PayloadAction<{ id: string }>) => {
            if (!state.selectedSurvey) return;
            const { id } = action.payload;
            state.selectedSurvey.pages[state.currentQuestionPageIndex].questions =
                state.selectedSurvey.pages[0].questions.filter((question) => question.id !== id);
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
    addAnotherOption,
    deleteQuestion,
    duplicateQuestion,
    addPage,
} = surveySlice.actions;
export default surveySlice.reducer;
