import { Text } from '@hh.ru/magritte-ui';

type SurveyCreateCardProps = {
    onClick: () => void;
};

export function SurveyCreateCard({ onClick }: SurveyCreateCardProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            className="survey-list-item-button"
        >
            <svg width="250" height="200" viewBox="0 0 250 200" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="1.5" y="1.5" width="247" height="197" rx="28.5" stroke="#768694" strokeWidth="3" />
                <path d="M76.4858 99.4292L176.481 98.4858" stroke="#768694" strokeWidth="4" strokeLinecap="round" />
                <path d="M125 148V48" stroke="#768694" strokeWidth="4" strokeLinecap="round" />
            </svg>

            <Text typography="subtitle-3-semibold" style="primary">
                Создать опрос
            </Text>
        </button>
    );
}
