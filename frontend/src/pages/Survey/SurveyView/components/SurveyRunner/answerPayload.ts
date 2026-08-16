import type { Question } from '@/shared/types/Question.type';
import type { SurveyAnswerRequest } from '@/api/surveyResponses';

export const OTHER_OPTION_VALUE = '__other__';

export type AnswerValue = string | string[] | boolean;
export type Answers = Record<string, AnswerValue>;
export type AnswerErrors = Record<string, string>;

export function isQuestionAnswered(question: Question, value: AnswerValue | undefined, otherText?: string) {
    switch (question.type) {
        case 'MULTIPLE_CHOICE':
            return (
                Array.isArray(value) &&
                value.length > 0 &&
                (!value.includes(OTHER_OPTION_VALUE) || Boolean(otherText?.trim()))
            );
        case 'YES_NO':
            return typeof value === 'boolean';
        case 'SHORT_TEXT':
        case 'LONG_TEXT':
        case 'DATE':
        case 'TIME':
            return typeof value === 'string' && value.trim().length > 0;
        case 'SINGLE_CHOICE':
            return (
                typeof value === 'string' &&
                value.trim().length > 0 &&
                (value !== OTHER_OPTION_VALUE || Boolean(otherText?.trim()))
            );
    }
}

export function buildAnswerPayload(
    question: Question,
    value: AnswerValue | undefined,
    otherText: string,
): SurveyAnswerRequest {
    switch (question.type) {
        case 'SINGLE_CHOICE':
            return value === OTHER_OPTION_VALUE
                ? { textValue: otherText.trim() }
                : { selectedAnswerOptionIds: typeof value === 'string' && value ? [value] : [] };
        case 'MULTIPLE_CHOICE': {
            const selectedIds = Array.isArray(value) ? value : [];
            const selectedAnswerOptionIds = selectedIds.filter((id) => id !== OTHER_OPTION_VALUE);
            const trimmedOtherText = otherText.trim();
            return selectedIds.includes(OTHER_OPTION_VALUE) && trimmedOtherText
                ? { selectedAnswerOptionIds, textValue: trimmedOtherText }
                : { selectedAnswerOptionIds };
        }
        case 'YES_NO':
            return { booleanValue: typeof value === 'boolean' ? value : undefined };
        case 'DATE':
            return { dateValue: typeof value === 'string' && value ? value : undefined };
        case 'TIME':
            return { timeValue: typeof value === 'string' && value ? value : undefined };
        case 'SHORT_TEXT':
        case 'LONG_TEXT':
            return { textValue: typeof value === 'string' ? value.trim() : '' };
    }
}
