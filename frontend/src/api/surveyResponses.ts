import { apiClient } from './client';

export type CreateSurveyResponseResult = {
    id: string;
};

export type SurveyAnswerResponse = {
    responseId: string;
    questionId: string;
    answerText: string;
};

export async function createSurveyResponse(surveyId: string): Promise<CreateSurveyResponseResult> {
    const { data } = await apiClient.post<CreateSurveyResponseResult>(`/api/surveys/${surveyId}/responses`);

    return data;
}

export async function createSurveyAnswer(
    responseId: string,
    questionId: string,
    answerText: string,
): Promise<SurveyAnswerResponse> {
    const { data } = await apiClient.post<SurveyAnswerResponse>(`/api/responses/${responseId}/answers`, {
        answerText,
    }, {
        params: { questionId },
    });

    return data;
}

export async function completeSurveyResponse(responseId: string): Promise<void> {
    await apiClient.post(`/api/responses/${responseId}/complete`);
}
