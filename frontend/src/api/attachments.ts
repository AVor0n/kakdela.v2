import type { Question } from '@/shared/types/Question.type';
import { apiClient } from './client';

export async function attachImageToQuestion(questionId: string, imageFile: File): Promise<Question> {
    const formData = new FormData();
    formData.append('file', imageFile);

    const { data } = await apiClient.post<Question>(`/api/questions/${questionId}/attachment`, formData);

    return data;
}

export async function updateAttachmentOfQuestion(questionId: string, imageFile: File): Promise<Question> {
    const formData = new FormData();
    formData.append('file', imageFile);

    const { data } = await apiClient.put<Question>(`/api/questions/${questionId}/attachment`, formData);

    return data;
}

export async function removeImageFromQuestion(questionId: string): Promise<void> {
    await apiClient.delete<Question>(`/api/questions/${questionId}/attachment`);
}
