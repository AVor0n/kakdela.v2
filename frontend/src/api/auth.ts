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
