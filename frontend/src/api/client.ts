import axios from 'axios';
import { routePatterns, routes } from '@/app/routes';
import { refreshToken } from './account';
import { matchPath } from 'react-router-dom';

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

const ANONYMOUS_ALLOWED_PATTERNS = [
    routePatterns.root,
    routePatterns.surveysView,
    routePatterns.authLogin,
    routePatterns.authRegister,
    routePatterns.resetPassword,
];

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
        const isAnonymousAllowedPage = ANONYMOUS_ALLOWED_PATTERNS.some((pattern) =>
            matchPath(pattern, window.location.pathname),
        );
        if (isUnauthorized && !isRefreshRequest && !isAnonymousAllowedPage) {
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
