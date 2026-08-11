import type { Survey } from '@/shared/types/Survey.type';
import { apiClient } from './client';
import type { Template } from '@/shared/types/Survey.type';
type TemplateResponse = Template;

type UpdateTemplateRequest = Partial<Pick<TemplateResponse, 'title' | 'description' | 'published'>>;

export async function getMyTemplates(): Promise<TemplateResponse[]> {
    const { data } = await apiClient.get<TemplateResponse[]>('/api/accounts/me/templates');

    return data;
}

export async function getTemplateById(templateId: string): Promise<TemplateResponse> {
    const { data } = await apiClient.get<TemplateResponse>(`/api/templates/${templateId}`);

    return data;
}

export async function createTemplateFromSurvey(surveyId: string): Promise<TemplateResponse> {
    const { data } = await apiClient.post<TemplateResponse>(`/api/surveys/${surveyId}/create-template`);

    return data;
}

export async function createSurveyFromTemplate(templateId: string): Promise<Survey> {
    const { data } = await apiClient.post<Survey>(`/api/templates/${templateId}/create-survey`);

    return data;
}

export async function updateTemplate(templateId: string, request: UpdateTemplateRequest): Promise<TemplateResponse> {
    const { data } = await apiClient.patch<TemplateResponse>(`/api/templates/${templateId}`, request);

    return data;
}

export async function deleteTemplate(templateId: string): Promise<void> {
    await apiClient.delete(`/api/templates/${templateId}`);
}
