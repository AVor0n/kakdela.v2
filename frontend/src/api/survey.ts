import { apiClient } from '@/api/client';
import type { Survey, SurveyListItem } from '@/shared/types/Survey.type';

export type SurveyResponse = Survey;
export type UpdateSurveyRequest = Partial<
    Pick<
        Survey,
        | 'title'
        | 'description'
        | 'isAuthorizedOnly'
        | 'doNotify'
        | 'expireAt'
        | 'isLimitedToOneResponse'
        | 'isPublished'
    >
>;

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

export async function updateSurvey(surveyId: string, updateData: UpdateSurveyRequest): Promise<SurveyResponse> {
    const { data } = await apiClient.put<SurveyResponse>(`/api/surveys/${surveyId}`, updateData);

    return data;
}

export async function deleteSurvey(surveyId: string): Promise<void> {
    await apiClient.delete(`/surveys/${surveyId}`);
}
