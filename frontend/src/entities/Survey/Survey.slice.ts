import type { ClosingPage, Survey, SurveyListItem } from '@/shared/types/Survey.type';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export interface ISurveyState {
    surveys: SurveyListItem[];
    selectedSurvey: Survey | null;
}

const initialState: ISurveyState = {
    surveys: [],
    selectedSurvey: null,
};

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

        setClosingPage: (state, action: PayloadAction<{ closingPage: ClosingPage | null }>) => {
            if (!state.selectedSurvey) return;
            state.selectedSurvey.closingPage = action.payload.closingPage;
        },
        patchClosingPage: (state, action: PayloadAction<Partial<ClosingPage>>) => {
            if (!state.selectedSurvey?.closingPage) return;
            Object.assign(state.selectedSurvey.closingPage, action.payload);
        },

        deleteSurvey: (state, action: PayloadAction<{ surveyId: string }>) => {
            const { surveyId } = action.payload;
            state.surveys = state.surveys.filter((survey) => survey.id !== surveyId);
            if (state.selectedSurvey?.id === surveyId) {
                state.selectedSurvey = null;
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

export const { setSurveys, setSelectedSurvey, setClosingPage, patchClosingPage, deleteSurvey, addSurvey } =
    surveySlice.actions;
export default surveySlice.reducer;
