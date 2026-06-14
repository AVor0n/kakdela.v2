import { Box, Title } from '@hh.ru/magritte-ui';
import { useNavigate } from 'react-router-dom';
import { routes } from '@/app/routes';
import { mockSurveys } from '@/pages/Survey/components/SurveyList/mockSurveys';
import { SurveyCreateCard } from '@/pages/Survey/components/SurveyList/SurveyCreateCard';
import { SurveyItem } from '@/pages/Survey/components/SurveyList/SurveyItem';
import './SurveyList.css';

export function SurveyList() {
    const navigate = useNavigate();

    const handleCreateClick = () => {
        navigate(routes.surveyCreate());
    };

    const handleSurveyClick = (surveyId: string) => {
        navigate(routes.surveyView(surveyId));
    };

    return (
        <div className="survey-list-page">
            <div className="survey-list-content">
                <Title Element="h1" size="large">
                    Список опросов
                </Title>

                <Box p={24} className="survey-list-card">
                    <div className="survey-list-grid">
                        <SurveyCreateCard onClick={handleCreateClick} />

                        {mockSurveys.map((survey) => (
                            <SurveyItem key={survey.id} survey={survey} onClick={() => handleSurveyClick(survey.id)} />
                        ))}
                    </div>
                </Box>
            </div>
        </div>
    );
}
