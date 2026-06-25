import { apiClient } from '@/api/client';
import type { Survey, SurveyListItem } from '@/shared/types/Survey.type';

export type SurveyResponse = Survey;

export async function createSurvey(): Promise<SurveyResponse> {
    const { data } = await apiClient.post<SurveyResponse>('/api/surveys', {
        title: 'Новый опрос',
    });

    return data;
}

export async function getSurveyById(surveyId: string): Promise<SurveyResponse> {
    const { data } = await apiClient.get<SurveyResponse>(`/api/surveys/${surveyId}`);

    return data;
}

export async function getMySurveys(): Promise<SurveyListItem[]> {
    const { data } = await apiClient.get<SurveyListItem[]>('/api/accounts/me/surveys');

    return data;
}

export async function getSurveys(): Promise<SurveyListItem[]> {
    const { data } = await apiClient.get<SurveyListItem[]>('/api/surveys');

    return data;
}
