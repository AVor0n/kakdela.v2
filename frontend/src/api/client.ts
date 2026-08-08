import axios from 'axios';
import { routes } from '@/app/routes';
import { refreshToken } from './account';

export const apiClient = axios.create({
    withCredentials: true,
});

let refreshPromise: Promise<void> | null = null;

function getRefreshPromise(): Promise<void> {
    if (!refreshPromise) {
        refreshPromise = refreshToken().finally(() => {
            refreshPromise = null;
        });
    }
    return refreshPromise;
}

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (!axios.isAxiosError(error)) {
            return Promise.reject(error);
        }

        const status = error.response?.status;
        const requestUrl = error.config?.url ?? '';
        const isRefreshRequest = requestUrl.includes('/api/auth/refresh');
        const isUnauthorized = status === 401 || status === 403;

        if (isUnauthorized && !isRefreshRequest && window.location.pathname !== routes.login()) {
            try {
                await getRefreshPromise();
                return apiClient(error.config!);
            } catch {
                window.location.assign(routes.login());
            }
        }

        return Promise.reject(error);
    },
);
