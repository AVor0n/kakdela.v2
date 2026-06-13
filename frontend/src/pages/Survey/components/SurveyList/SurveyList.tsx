import { Box, Title } from '@hh.ru/magritte-ui';
import { mockSurveys } from '@/pages/Survey/components/SurveyList/mockSurveys';
import { SurveyCreateCard } from '@/pages/Survey/components/SurveyList/SurveyCreateCard';
import { SurveyItem } from '@/pages/Survey/components/SurveyList/SurveyItem';

export function SurveyList() {
    const handleCreateClick = () => {
        // TODO: перейти к созданию опроса, когда экран будет готов.
    };

    const handleSurveyClick = () => {
        // TODO: перейти к опросу, когда экран будет готов.
    };

    return (
        <div
            style={{
                width: '100%',
                minHeight: '100dvh',
                backgroundColor: '#f4f6f8',
                padding: '40px 24px',
            }}
        >
            <div
                style={{
                    width: '100%',
                    maxWidth: 1180,
                    margin: '0 auto',
                }}
            >
                <Title Element="h1" size="large">
                    Список опросов
                </Title>

                <Box
                    p={24}
                    style={{
                        width: '100%',
                        boxSizing: 'border-box',
                        marginTop: 20,
                        backgroundColor: 'white',
                        borderRadius: '24px',
                        border: '1px solid #eee',
                    }}
                >
                    <div
                        style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fit, minmax(min(220px, 100%), 1fr))',
                            gap: 24,
                            width: '100%',
                        }}
                    >
                        <SurveyCreateCard onClick={handleCreateClick} />

                        {mockSurveys.map((survey) => (
                            <SurveyItem key={survey.id} survey={survey} onClick={handleSurveyClick} />
                        ))}
                    </div>
                </Box>
            </div>
        </div>
    );
}
