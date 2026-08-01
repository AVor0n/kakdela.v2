import { Button, Text } from '@hh.ru/magritte-ui';

type SurveyCreateCardProps = {
    onClick: () => void;
};

export function SurveyCreateCard({ onClick }: SurveyCreateCardProps) {
    return (
        <Button mode='secondary' type='button' onClick={onClick}>
            <Text typography='subtitle-1-semibold' style='primary'>
                Создать опрос
            </Text>
        </Button>
    );
}
