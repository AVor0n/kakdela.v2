import type { SurveyAnswerRequest } from '@/api/surveyResponses';

export function isEmptyAnswerPayload(payload: SurveyAnswerRequest) {
    return (
        (payload.selectedAnswerOptionIds?.length ?? 0) === 0 &&
        !payload.textValue?.trim() &&
        payload.booleanValue === undefined &&
        !payload.dateValue?.trim() &&
        !payload.timeValue?.trim()
    );
}
