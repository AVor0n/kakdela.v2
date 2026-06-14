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
        setSurveys: (state, action) => {
            state.surveys = action.payload;
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
                    question.answerOptions?.push(`Вариант ${question.answerOptions.length}`);
                }
            });
        },
        deleteOption: (state, action: PayloadAction<{ removeValue: string }>) => {
            if (!state.selectedSurvey) return;
            const { removeValue } = action.payload;
            state.selectedSurvey.pages[0].questions.forEach((question) => {
                if (state.selectedQuestion && question.id === state.selectedQuestion.id) {
                    question.answerOptions = question.answerOptions?.filter((value) => {
                        return value !== removeValue;
                    });
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
    addQuestionOptions,
    setSelectedQuestion,
    deleteOption,
} = surveySlice.actions;
export default surveySlice.reducer;
