import axios from 'axios';
import { useEffect, useState } from 'react';
import { Box, Text, Title } from '@hh.ru/magritte-ui';
import { useNavigate } from 'react-router-dom';
import { getMySurveys } from '@/api/surveys';
import { routes } from '@/app/routes';
import { SurveyCreateCard } from '@/pages/Survey/components/SurveyList/SurveyCreateCard';
import { SurveyItem } from '@/pages/Survey/components/SurveyList/SurveyItem';
import type { SurveyShortResponse } from '@/pages/Survey/components/SurveyList/types';
import './SurveyList.css';

export function SurveyList() {
    const navigate = useNavigate();
    const [surveys, setSurveys] = useState<SurveyShortResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        let isMounted = true;

        const loadSurveys = async () => {
            try {
                setError('');
                const mySurveys = await getMySurveys();

                if (isMounted) {
                    setSurveys(mySurveys);
                }
            } catch (requestError) {
                const status = axios.isAxiosError(requestError) ? requestError.response?.status : undefined;

                if (
                    isMounted &&
                    status !== 401 &&
                    status !== 403
                ) {
                    setError('Не удалось загрузить список опросов');
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false);
                }
            }
        };

        loadSurveys();

        return () => {
            isMounted = false;
        };
    }, []);

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
                    {isLoading ? (
                        <div className="survey-list-message">
                            <Text typography="paragraph-2-regular" style="secondary">
                                Загружаем опросы
                            </Text>
                        </div>
                    ) : error ? (
                        <div className="survey-list-message">
                            <Text typography="paragraph-2-regular" style="negative">
                                {error}
                            </Text>
                        </div>
                    ) : (
                        <div className="survey-list-grid">
                            <SurveyCreateCard onClick={handleCreateClick} />

                            {surveys.map((survey) => (
                                <SurveyItem
                                    key={survey.id}
                                    survey={survey}
                                    onClick={() => handleSurveyClick(survey.id)}
                                />
                            ))}
                        </div>
                    )}
                </Box>
            </div>
        </div>
    );
}
