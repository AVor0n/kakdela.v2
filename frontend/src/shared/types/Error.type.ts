export type ApiErrorCode =
    | 'NOT_ALL_MANDATORY_QUESTIONS_ANSWERED'
    | 'CONDITIONS_OF_PAGE_HAVE_CONFLICTS'
    | 'ACCESS_DENIED'
    | 'BAD_CREDENTIALS'
    | 'ACCOUNT_DELETED'
    | 'RESOURCE_NOT_FOUND'
    | 'ENTITY_NOT_FOUND'
    | 'BAD_REQUEST_BODY'
    | 'INTERNAL_SERVER'
    | 'UNDEFINED';

export type ApiErrorResponse = {
    timestamp: string;
    internalErrorCode: ApiErrorCode;
    errorId: string;
    message: string;
    object1Id: string | null;
    object2Id: string | null;
    path: string | null;
};

export type Error = Pick<ApiErrorResponse, 'message'>;
