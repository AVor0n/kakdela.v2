import { apiClient } from '@/api/client';

export type LoginRequest = {
    login: string;
    password: string;
};

export type LoginResponse = {
    accessToken: string;
    refreshToken: null;
};

export type RegisterRequest = {
    login: string;
    email: string;
    password: string;
    passwordConfirmation: string;
};

export async function login(payload: LoginRequest): Promise<LoginResponse> {
    const { data } = await apiClient.post<LoginResponse>('/api/auth/login', payload);

    return data;
}

export async function register(payload: RegisterRequest): Promise<string> {
    const { data } = await apiClient.post<string>('/api/auth/register', payload);

    return data;
}
