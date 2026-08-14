import { apiClient } from '@/api/client';

export type LoginRequest = {
    login: string;
    password: string;
};

export type RegisterRequest = {
    login: string;
    email: string;
    password: string;
    passwordConfirmation: string;
};

export async function login(payload: LoginRequest): Promise<void> {
    await apiClient.post('/api/auth/login', payload);
}

export async function register(payload: RegisterRequest): Promise<string> {
    const { data } = await apiClient.post<string>('/api/auth/register', payload);

    return data;
}

export type ResetPasswordRequest = {
    email: string;
    code: string;
    newPassword: string;
    passwordConfirmation: string;
};

export async function forgotPassword(email: string): Promise<void> {
    await apiClient.post('/api/auth/forgot-password', null, { params: { email } });
}

export async function verifyResetCode(email: string, code: string): Promise<void> {
    await apiClient.get('/api/auth/verify-reset-code', { params: { email, code } });
}

export async function resetPassword(payload: ResetPasswordRequest): Promise<void> {
    await apiClient.patch('/api/auth/reset-password', null, { params: payload });
}
