import axios, { type InternalAxiosRequestConfig } from 'axios';
import { routePatterns, routes } from '@/app/routes';
import { getApiError } from '@/shared/utils/apiError';
import { refreshToken } from './refresh';
import { matchPath } from 'react-router-dom';

type RetryableRequestConfig = InternalAxiosRequestConfig & {
    _retry?: boolean;
};

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
    routePatterns.forgotPassword,
];

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (!axios.isAxiosError(error)) {
            return Promise.reject(error);
        }

        const requestConfig = error.config as RetryableRequestConfig | undefined;
        const requestUrl = error.config?.url ?? '';
        const isAuthRequest = requestUrl.includes('/api/auth/');
        const isExpiredAccessToken = getApiError(error)?.internalErrorCode === 'EXPIRED_ACCESS_TOKEN';
        const isAnonymousAllowedPage = ANONYMOUS_ALLOWED_PATTERNS.some((pattern) =>
            matchPath(pattern, window.location.pathname),
        );

        if (isExpiredAccessToken && !isAuthRequest && requestConfig && !requestConfig._retry) {
            requestConfig._retry = true;

            try {
                await getRefreshPromise();
                return apiClient(requestConfig);
            } catch {
                if (!isAnonymousAllowedPage) {
                    window.location.assign(routes.login());
                }
            }
        }

        return Promise.reject(error);
    },
);
