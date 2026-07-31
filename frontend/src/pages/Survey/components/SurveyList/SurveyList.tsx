import axios from 'axios';
import { useEffect, useState } from 'react';
import { Box, Text, Title } from '@hh.ru/magritte-ui';
import { useNavigate } from 'react-router-dom';
import { createSurvey, getMySurveys } from '@/api/survey';
import { routes } from '@/app/routes';
import { SurveyCreateCard } from '@/pages/Survey/components/SurveyList/SurveyCreateCard';
import { SurveyItem } from '@/pages/Survey/components/SurveyList/components/SurveyItem/SurveyItem';
import type { SurveyListItem } from '@/shared/types/Survey.type';
import styles from './SurveyList.module.css';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { clearErrorMessage, setErrorMessage } from '@/entities/Error/Error.slice';
import { AccountDetail } from '@/shared/ui/AccountDetail/AccountDetail';
import { useAppSelector } from '@/hooks/useAppSelector';
import { setSurveys } from '@/entities/Survey/Survey.slice';

export function SurveyList() {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(true);
    const { surveys } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();

    useEffect(() => {
        let isMounted = true;

        const loadSurveys = async () => {
            try {
                dispatch(clearErrorMessage());
                const mySurveys = await getMySurveys();

                if (isMounted) {
                    dispatch(setSurveys({ surveys: mySurveys }));
                }
            } catch (requestError) {
                const status = axios.isAxiosError(requestError) ? requestError.response?.status : undefined;

                if (isMounted && status !== 401 && status !== 403) {
                    dispatch(setErrorMessage({ message: 'Не удалось загрузить список опросов' }));
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
        createSurvey()
            .then((data) => {
                navigate(routes.surveyQuestions(data.id));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: 'Не удалось создать опрос' }));
                }
            });
    };

    const handleSurveyClick = (survey: SurveyListItem) => {
        navigate(survey.userRole === 'ANALYST' ? routes.surveyAnswers(survey.id) : routes.surveyQuestions(survey.id));
    };

    return (
        <div className={styles.page}>
            <div className={styles.avatar}>
                <AccountDetail />
            </div>
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
                    ) : (
                        <div className={styles.grid}>
                            <div className={styles.createButtonWrapper}>
                                <SurveyCreateCard onClick={handleCreateClick} />
                            </div>
                            {surveys.map((survey) => (
                                <SurveyItem key={survey.id} survey={survey} onClick={() => handleSurveyClick(survey)} />
                            ))}
                        </div>
                    )}
                </Box>
            </div>
        </div>
    );
}
