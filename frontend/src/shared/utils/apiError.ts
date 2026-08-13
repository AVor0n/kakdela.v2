import axios from 'axios';
import type { ApiErrorCode, ApiErrorResponse } from '@/shared/types/Error.type';

type RawApiError = Partial<ApiErrorResponse> & {
    errorCode?: unknown;
    object1id?: unknown;
    object2id?: unknown;
};

function nullableString(value: unknown): string | null {
    return typeof value === 'string' ? value : null;
}

export function getApiError(error: unknown): ApiErrorResponse | null {
    if (!axios.isAxiosError(error) || !error.response?.data || typeof error.response.data !== 'object') {
        return null;
    }

    const data = error.response.data as RawApiError;
    const internalErrorCode = data.internalErrorCode ?? data.errorCode;
    if (typeof internalErrorCode !== 'string' || typeof data.message !== 'string') {
        return null;
    }

    return {
        timestamp: typeof data.timestamp === 'string' ? data.timestamp : '',
        internalErrorCode: internalErrorCode as ApiErrorCode,
        errorId: typeof data.errorId === 'string' ? data.errorId : '',
        message: data.message,
        object1Id: nullableString(data.object1Id ?? data.object1id),
        object2Id: nullableString(data.object2Id ?? data.object2id),
        path: nullableString(data.path),
    };
}
