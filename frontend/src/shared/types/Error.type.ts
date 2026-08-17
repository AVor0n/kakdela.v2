export type ApiErrorCode =
    | 'NOT_ALL_MANDATORY_QUESTIONS_ANSWERED'
    | 'CONDITIONS_OF_PAGE_HAVE_CONFLICTS'
    | 'CONDITION_FOR_THESE_PAGE_AND_NEXT_PAGE_ALREADY_EXISTS'
    | 'SURVEY_IS_EMPTY'
    | 'ACCESS_DENIED'
    | 'BAD_CREDENTIALS'
    | 'ACCOUNT_DELETED'
    | 'RESOURCE_NOT_FOUND'
    | 'ENTITY_NOT_FOUND'
    | 'BAD_REQUEST_BODY'
    | 'INTERNAL_SERVER'
    | 'EXPIRED_ACCESS_TOKEN'
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
