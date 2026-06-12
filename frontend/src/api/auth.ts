import axios from 'axios';

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
    const { data } = await axios.post<LoginResponse>('/api/auth/login', payload);

    return data;
}

export async function register(payload: RegisterRequest): Promise<string> {
    const { data } = await axios.post<string>('/api/auth/register', payload);

    return data;
}
