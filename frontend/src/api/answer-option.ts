import { apiClient } from './client';

export type AnswerOptionResponse = {
    id: string;
    text: string;
    serialNumber: number;
};

export type CreateAnswerOptionRequest = {
    text: string;
    serialNumber: number;
};

export type UpdateAnswerOptionRequest = Partial<CreateAnswerOptionRequest>;

export async function addAnswerOption(
    questionId: string,
    payload: CreateAnswerOptionRequest,
): Promise<AnswerOptionResponse> {
    const { data } = await apiClient.post(`/api/questions/${questionId}/answer-options`, payload);

    return data;
}

export async function updateAnswerOption(
    answerOptionId: string,
    payload: UpdateAnswerOptionRequest,
): Promise<AnswerOptionResponse> {
    const { data } = await apiClient.put(`/api/answer-options/${answerOptionId}`, payload);

    return data;
}

export async function deleteAnswerOption(answerOptionId: string): Promise<void> {
    await apiClient.delete(`/api/answer-options/${answerOptionId}`);
}
