import type { Question } from '@/shared/types/Question.type';
import { apiClient } from './client';

export type UpdateQuestionRequest = Partial<
    Pick<
        Question,
        'text' | 'isMandatory' | 'type' | 'description' | 'serialNumber' | 'hasOtherOption' | 'answerOptionOrder'
    >
>;

export type CreateQuestionRequest = Pick<Question, 'text' | 'serialNumber' | 'type'>;

export async function createQuestion(pageId: string, questionData: CreateQuestionRequest): Promise<Question> {
    const { data } = await apiClient.post<Question>(`/api/pages/${pageId}/questions`, questionData);

    return data;
}

export async function cloneQuestion(questionId: string): Promise<Question> {
    const { data } = await apiClient.post<Question>(`/api/questions/${questionId}/clone`);

    return data;
}

export async function updateQuestion(questionId: string, updateData: UpdateQuestionRequest): Promise<Question> {
    const { data } = await apiClient.put<Question>(`/api/questions/${questionId}`, updateData);

    return data;
}

export async function deleteQuestion(questionId: string): Promise<void> {
    await apiClient.delete(`/api/questions/${questionId}`);
}
