import { apiClient } from './client';

export type SurveyPermissionRole = 'ANALYST' | 'EDITOR';

export type PermissionAccount = {
    id: string;
    login: string;
    email: string;
};

export type SurveyPermission = {
    surveyId: string;
    account: PermissionAccount;
    role: SurveyPermissionRole;
    doNotify: boolean;
};

type CreateSurveyPermissionRequest = {
    email: string;
    role: SurveyPermissionRole;
};

export async function getSurveyPermissions(surveyId: string): Promise<SurveyPermission[]> {
    const { data } = await apiClient.get<SurveyPermission[]>(`/api/surveys/${surveyId}/permissions`);

    return data;
}

export async function createSurveyPermission(
    surveyId: string,
    request: CreateSurveyPermissionRequest,
): Promise<SurveyPermission> {
    const { data } = await apiClient.post<SurveyPermission>(`/api/surveys/${surveyId}/permissions`, request);

    return data;
}

export async function updateSurveyPermissionRole(
    surveyId: string,
    accountId: string,
    role: SurveyPermissionRole,
): Promise<SurveyPermission> {
    const { data } = await apiClient.put<SurveyPermission>(
        `/api/surveys/${surveyId}/permissions`,
        { role },
        { params: { accountId } },
    );

    return data;
}

export async function deleteSurveyPermission(surveyId: string, accountId: string): Promise<void> {
    await apiClient.delete(`/api/surveys/${surveyId}/permissions`, {
        params: { accountId },
    });
}
