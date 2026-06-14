import axios from 'axios';
import { store } from '@/app/store';
import { routes } from '@/app/routes';
import { clearAccessToken } from '@/features/auth/authSlice';

export const apiClient = axios.create();

apiClient.interceptors.request.use((config) => {
    const token = store.getState().auth.accessToken;

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (axios.isAxiosError(error) && error.response?.status === 401) {
            store.dispatch(clearAccessToken());

            const requestUrl = error.config?.url ?? '';
            const isAuthRequest = requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register');

            if (!isAuthRequest && window.location.pathname !== routes.login()) {
                window.location.assign(routes.login());
            }
        }

        return Promise.reject(error);
    },
);
