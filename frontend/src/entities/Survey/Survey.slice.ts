import type { Question, QuestionType } from '@/shared/types/Question.type';
import type { Survey } from '@/shared/types/Survey.type';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export interface ISurveyState {
    surveys: Survey[];
    selectedQuestion: Question | null;
    selectedSurvey: Survey | null;
}

const initialState: ISurveyState = {
    surveys: [],
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
        updateQuestionTitle: (state, action: PayloadAction<{ id: string; title: string }>) => {
            if (!state.selectedSurvey) return;
            const { id, title } = action.payload;
            state.selectedSurvey.pages[0].questions.map((question) => {
                if (question.id === id) {
                    question.title = title;
                }
            });
        },
        setSelectedQuestion: (state, action: PayloadAction<{ question: Question }>) => {
            const { question } = action.payload;
            state.selectedQuestion = question;
        },
        updateQuestionType: (state, action: PayloadAction<{ type: QuestionType }>) => {
            if (state.selectedSurvey === null) return;
            const { type } = action.payload;
            state.selectedSurvey.pages[0].questions.map((question) => {
                if (state.selectedQuestion) {
                    if (question.id === state.selectedQuestion.id) {
                        question.type = type;
                    }
                }
            });
        },
        addQuestionOptions: (state) => {
            if (!state.selectedSurvey) return;
            state.selectedSurvey.pages[0].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.answerOptions?.push(`Вариант ${question.answerOptions.length + 1}`);
                }
            });
        },
        addAnotherOption: (state) => {
            if (!state.selectedSurvey) return;
            state.selectedSurvey.pages[0].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.answerOptions?.push('Другое');
                }
            });
        },
        deleteOption: (state, action: PayloadAction<{ removeIndex: number }>) => {
            if (!state.selectedSurvey) return;
            const { removeIndex } = action.payload;
            state.selectedSurvey.pages[0].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.answerOptions = question.answerOptions?.filter((_, index) => index !== removeIndex);
                }
            });
        },
        addQuestion: (state) => {
            if (!state.selectedSurvey) return;
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
            state.selectedSurvey.pages[0].questions.push(newQuestion);
        },
        setMandatory: (state) => {
            if (!state.selectedSurvey) return;
            state.selectedSurvey.pages[0].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.mandatory = !question.mandatory;
                }
            });
        },
        setOptionValue: (state, action: PayloadAction<{ value: string; index: number }>) => {
            if (!state.selectedSurvey) return;
            const { value, index } = action.payload;
            state.selectedSurvey.pages[0].questions.forEach((question) => {
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
            state.selectedSurvey.pages[0].questions = state.selectedSurvey.pages[0].questions.filter(
                (question) => question.id !== id,
            );
        },
        duplicateQuestion: (state, action: PayloadAction<{ id: string }>) => {
            if (!state.selectedSurvey) return;
            const { id } = action.payload;
            const questionToDuplicate = state.selectedSurvey.pages[0].questions.find((question) => question.id === id);
            const index = state.selectedSurvey.pages[0].questions.findIndex((question) => question.id === id);
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
} = surveySlice.actions;
export default surveySlice.reducer;
