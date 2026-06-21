import { apiClient } from '@/api/client';
import type { SurveyShortResponse } from '@/pages/Survey/components/SurveyList/types';

export async function getMySurveys(): Promise<SurveyShortResponse[]> {
    const { data } = await apiClient.get<SurveyShortResponse[]>('/api/accounts/me/surveys');

    return data;
}
