import axios from 'axios';
import { useEffect, useState } from 'react';
import { Box, Text, Title } from '@hh.ru/magritte-ui';
import { useNavigate } from 'react-router-dom';
import { createSurvey, getMySurveys } from '@/api/survey';
import { routes } from '@/app/routes';
import { SurveyCreateCard } from '@/pages/Survey/components/SurveyList/SurveyCreateCard';
import { SurveyItem } from '@/pages/Survey/components/SurveyList/SurveyItem';
import type { SurveyListItem } from '@/shared/types/Survey.type';
import styles from './SurveyList.module.css';

export function SurveyList() {
    const navigate = useNavigate();
    const [surveys, setSurveys] = useState<SurveyListItem[]>([]);
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

                if (isMounted && status !== 401 && status !== 403) {
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
        createSurvey().then((data) => {
            navigate(routes.surveyEdit(data.id));
        });
    };

    const handleSurveyClick = (surveyId: string) => {
        navigate(routes.surveyQuestions(surveyId));
    };

    return (
        <div className={styles.page}>
            <div className={styles.content}>
                <Title Element='h1' size='large'>
                    Список опросов
                </Title>

                <Box p={24} className={styles.card}>
                    {isLoading ? (
                        <div className={styles.message}>
                            <Text typography='paragraph-2-regular' style='secondary'>
                                Загружаем опросы
                            </Text>
                        </div>
                    ) : error ? (
                        <div className={styles.message}>
                            <Text typography='paragraph-2-regular' style='negative'>
                                {error}
                            </Text>
                        </div>
                    ) : (
                        <div className={styles.grid}>
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
