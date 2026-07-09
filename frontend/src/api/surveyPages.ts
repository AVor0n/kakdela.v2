import type { Page } from '@/shared/types/Survey.type';
import { apiClient } from '@/api/client';

export type PageResponse = Page;
export type UpdateSurveyPageRequest = Partial<Pick<Page, 'serialNumber' | 'title' | 'description'>>;

export async function createSurveyPage(surveyId: string, serialNumber: number): Promise<PageResponse> {
    const { data } = await apiClient.post<PageResponse>(`/api/surveys/${surveyId}/pages`, { serialNumber });

    return data;
}

export async function updateSurveyPage(pageId: string, payload: UpdateSurveyPageRequest): Promise<PageResponse> {
    const { data } = await apiClient.put<PageResponse>(`/api/pages/${pageId}`, payload);

    return data;
}

export async function deleteSurveyPage(pageId: string): Promise<void> {
    await apiClient.delete(`/api/pages/${pageId}`);
}
