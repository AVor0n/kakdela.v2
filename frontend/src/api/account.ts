import type { Account } from '@/shared/types/Account.type';
import { apiClient } from './client';

export async function getAccountDetails(): Promise<Account> {
    const { data } = await apiClient.get<Account>('/api/accounts/me');

    return data;
}

export async function logout(): Promise<void> {
    await apiClient.post('/api/auth/logout');
}

export async function refreshToken(): Promise<void> {
    await apiClient.post('/api/auth/refresh');
}

export async function ssoConnect(): Promise<void> {
    await apiClient.get('/api/accounts/me/link-hh-sso/init');
}

export async function ssoConfirm(password?: string): Promise<void> {
    await apiClient.post('/api/accounts/me/link-hh-sso/confirm', { password });
}
