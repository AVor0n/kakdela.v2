import { apiClient } from './client';

const SURVEY_RESPONSE_REQUEST_TIMEOUT_MS = 15_000;

export type CreateSurveyResponseResult = {
    id: string;
};

export type SurveyAnswerResponse = {
    responseId: string;
    questionId: string;
    answerText: string;
};

export type SurveyCompletedResponse = {
    id: string;
    accountId: string | null;
    surveyId: string;
    isCompleted: boolean;
    receivedAt: string | null;
    answers: SurveyAnswerResponse[];
};

export type SurveyResponsesExport = {
    file: Blob;
    filename: string;
};

function decodeFilename(filename: string): string {
    try {
        return decodeURIComponent(filename);
    } catch {
        return filename;
    }
}

function getExportFilename(contentDisposition?: string): string {
    if (!contentDisposition) {
        return 'responses.xlsx';
    }

    const utf8Filename = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
    if (utf8Filename) {
        return decodeFilename(utf8Filename.replace(/^"|"$/g, ''));
    }

    const filename = contentDisposition.match(/filename="?([^";]+)"?/i)?.[1];
    return filename ? decodeFilename(filename) : 'responses.xlsx';
}

export async function createSurveyResponse(surveyId: string): Promise<CreateSurveyResponseResult> {
    const { data } = await apiClient.post<CreateSurveyResponseResult>(`/api/surveys/${surveyId}/responses`, undefined, {
        timeout: SURVEY_RESPONSE_REQUEST_TIMEOUT_MS,
    });

    return data;
}

export async function getSurveyResponses(surveyId: string): Promise<SurveyCompletedResponse[]> {
    const { data } = await apiClient.get<SurveyCompletedResponse[]>(`/api/surveys/${surveyId}/responses`);

    return data;
}

export async function exportSurveyResponses(surveyId: string): Promise<SurveyResponsesExport> {
    const { data, headers } = await apiClient.get<Blob>(`/api/surveys/${surveyId}/responses/export`, {
        responseType: 'blob',
    });

    return {
        file: data,
        filename: getExportFilename(headers['content-disposition']),
    };
}

export async function createSurveyAnswer(
    responseId: string,
    questionId: string,
    answerText: string,
): Promise<SurveyAnswerResponse> {
    const { data } = await apiClient.post<SurveyAnswerResponse>(
        `/api/responses/${responseId}/answers`,
        {
            answerText,
        },
        {
            params: { questionId },
            timeout: SURVEY_RESPONSE_REQUEST_TIMEOUT_MS,
        },
    );

    return data;
}

export async function updateSurveyAnswer(
    responseId: string,
    questionId: string,
    answerText: string,
): Promise<SurveyAnswerResponse> {
    const { data } = await apiClient.put<SurveyAnswerResponse>(
        `/api/responses/${responseId}/answers`,
        {
            answerText,
        },
        {
            params: { questionId },
            timeout: SURVEY_RESPONSE_REQUEST_TIMEOUT_MS,
        },
    );

    return data;
}

export async function deleteSurveyAnswer(responseId: string, questionId: string): Promise<void> {
    await apiClient.delete(`/api/responses/${responseId}/answers`, {
        params: { questionId },
        timeout: SURVEY_RESPONSE_REQUEST_TIMEOUT_MS,
    });
}

export async function completeSurveyResponse(responseId: string): Promise<void> {
    await apiClient.post(`/api/responses/${responseId}/complete`, undefined, {
        timeout: SURVEY_RESPONSE_REQUEST_TIMEOUT_MS,
    });
}
