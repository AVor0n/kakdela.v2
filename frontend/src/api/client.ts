import axios from 'axios';
import { routes } from '@/app/routes';

export const apiClient = axios.create({
    withCredentials: true,
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (axios.isAxiosError(error) && (error.response?.status === 401 || error.response?.status === 403)) {
            const requestUrl = error.config?.url ?? '';
            const isAccountCheck = requestUrl.includes('/api/accounts/me');

            if (!isAccountCheck && window.location.pathname !== routes.login()) {
                window.location.assign(routes.login());
            }
        }

        return Promise.reject(error);
    },
);
