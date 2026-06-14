import { Text } from '@hh.ru/magritte-ui';
import type { SurveyShortResponse } from '@/pages/Survey/components/SurveyList/types';

type SurveyItemProps = {
    survey: SurveyShortResponse;
    onClick: () => void;
};

export function SurveyItem({ survey, onClick }: SurveyItemProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            className="survey-list-item-button survey-list-item-button_centered"
        >
            <svg width="250" height="200" viewBox="0 0 250 200" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="1.5" y="1.5" width="247" height="197" rx="28.5" fill="#d5dce5" />
            </svg>

            <Text typography="subtitle-3-semibold" style="primary">
                {survey.title}
            </Text>
        </button>
    );
}
