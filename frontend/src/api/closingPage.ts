import { apiClient } from '@/api/client';
import type { ClosingPage, ClosingPageFile } from '@/shared/types/Survey.type';

export type ClosingPagePayload = {
    title?: string;
    description?: string;
    websiteUrl?: string;
};

type AttachmentUrlResponse = {
    attachmentUrl: string;
};

function createFileFormData(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return formData;
}

export async function createClosingPage(surveyId: string, payload: ClosingPagePayload): Promise<ClosingPage> {
    const { data } = await apiClient.post<ClosingPage>(`/api/surveys/${surveyId}/closing-page`, payload);
    return data;
}

export async function updateClosingPage(surveyId: string, payload: ClosingPagePayload): Promise<ClosingPage> {
    const { data } = await apiClient.patch<ClosingPage>(`/api/surveys/${surveyId}/closing-page`, payload);
    return data;
}

export async function getClosingPage(surveyId: string, responseId: string): Promise<ClosingPage> {
    const { data } = await apiClient.get<ClosingPage>(`/api/surveys/${surveyId}/closing-page`, {
        params: { responseId },
    });
    return data;
}

export async function deleteClosingPage(surveyId: string): Promise<void> {
    await apiClient.delete(`/api/surveys/${surveyId}/closing-page`);
}

async function uploadClosingPageAttachment<T>(url: string, file: File, replace: boolean): Promise<T> {
    const formData = createFileFormData(file);
    const { data } = replace ? await apiClient.put<T>(url, formData) : await apiClient.post<T>(url, formData);
    return data;
}

export function saveClosingPageImage(surveyId: string, file: File, replace: boolean): Promise<AttachmentUrlResponse> {
    return uploadClosingPageAttachment(`/api/surveys/${surveyId}/closing-page/media-attachment`, file, replace);
}

export async function deleteClosingPageImage(surveyId: string): Promise<void> {
    await apiClient.delete(`/api/surveys/${surveyId}/closing-page/media-attachment`);
}

export function saveClosingPageFile(surveyId: string, file: File, replace: boolean): Promise<ClosingPageFile> {
    return uploadClosingPageAttachment(`/api/surveys/${surveyId}/closing-page/attachment`, file, replace);
}

export async function deleteClosingPageFile(surveyId: string): Promise<void> {
    await apiClient.delete(`/api/surveys/${surveyId}/closing-page/attachment`);
}

export async function getClosingPageFileUrl(surveyId: string): Promise<string> {
    const { data } = await apiClient.get<AttachmentUrlResponse>(`/api/surveys/${surveyId}/closing-page/attachment`);
    return data.attachmentUrl;
}
