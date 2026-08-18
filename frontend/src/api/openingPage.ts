import { apiClient } from '@/api/client';

type AttachmentUrlResponse = {
    attachmentUrl: string;
};

function createImageFormData(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return formData;
}

export async function saveOpeningPageImage(
    surveyId: string,
    file: File,
    replace: boolean,
): Promise<AttachmentUrlResponse> {
    const url = `/api/surveys/${surveyId}/opening-page/media-attachment`;
    const formData = createImageFormData(file);
    const { data } = replace
        ? await apiClient.patch<AttachmentUrlResponse>(url, formData)
        : await apiClient.post<AttachmentUrlResponse>(url, formData);

    return data;
}

export async function deleteOpeningPageImage(surveyId: string): Promise<void> {
    await apiClient.delete(`/api/surveys/${surveyId}/opening-page/media-attachment`);
}
