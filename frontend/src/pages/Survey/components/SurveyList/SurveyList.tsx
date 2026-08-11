import axios from 'axios';
import { useEffect, useState } from 'react';
import { Box, Button, Text, Title } from '@hh.ru/magritte-ui';
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
import { getMyTemplates, getPublicTemplates } from '@/api/template';
import { setTemplates } from '@/entities/Template/Template.slice';
import { TemplateItem } from './components/TemplateItem/TemplateItem';
import classNames from 'classnames';

type TemplateKind = 'public' | 'local';

export function SurveyList() {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(true);
    const [templateIsLoading, setTemplateIsLoading] = useState<boolean>(false);
    const { surveys } = useAppSelector((state) => state.survey);
    const { templates } = useAppSelector((state) => state.template);
    const [templateKind, setTemplateKind] = useState<TemplateKind>('local');
    const dispatch = useAppDispatch();

    useEffect(() => {
        let isMounted = true;

        const loadSurveys = async () => {
            try {
                dispatch(clearErrorMessage());
                const mySurveys = await getMySurveys();
                const myTemplate = await getMyTemplates();

                if (isMounted) {
                    dispatch(setSurveys({ surveys: mySurveys }));
                    dispatch(setTemplates(myTemplate));
                }
            } catch (requestError) {
                const status = axios.isAxiosError(requestError) ? requestError.response?.status : undefined;

                if (isMounted && status !== 401 && status !== 403) {
                    dispatch(setErrorMessage({ message: 'Не удалось загрузить список опросов или список шаблонов' }));
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

    useEffect(() => {
        setTemplateIsLoading(true);
        if (templateKind === 'local') {
            getMyTemplates()
                .then((data) => {
                    dispatch(setTemplates(data));
                    setTemplateIsLoading(false);
                })
                .catch(() => dispatch(setErrorMessage({ message: 'Не удалось загрузить список ваших шаблонов' })));
        } else {
            getPublicTemplates()
                .then((data) => {
                    dispatch(setTemplates(data));
                    setTemplateIsLoading(false);
                })
                .catch(() => dispatch(setErrorMessage({ message: 'Не удалось загрузить список публичных шаблонов' })));
        }
    }, [templateKind]);

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
                    Шаблоны
                </Title>

                <Box p={24} className={styles.card}>
                    <div className={styles.kinds}>
                        <Button
                            mode={templateKind === 'local' ? 'primary' : 'secondary'}
                            style='accent'
                            onClick={() => setTemplateKind('local')}
                        >
                            Локальные
                        </Button>
                        <Button
                            mode={templateKind === 'public' ? 'primary' : 'secondary'}
                            style='accent'
                            onClick={() => setTemplateKind('public')}
                        >
                            Публичные
                        </Button>
                    </div>
                    {templateIsLoading ? (
                        <div className={styles.message}>
                            <Text typography='paragraph-2-regular' style='secondary'>
                                Загружаем шаблоны
                            </Text>
                        </div>
                    ) : (
                        <div className={classNames(styles.grid, styles.templates)}>
                            {templates.length !== 0 ? (
                                templates.map((template) => <TemplateItem key={template.id} template={template} />)
                            ) : (
                                <div className={styles.empty}>Нет ни одного шаблона</div>
                            )}
                        </div>
                    )}
                </Box>
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
                            {surveys.length !== 0 ? (
                                surveys.map((survey) => (
                                    <SurveyItem
                                        key={survey.id}
                                        survey={survey}
                                        onClick={() => handleSurveyClick(survey)}
                                    />
                                ))
                            ) : (
                                <div className={styles.empty}>Нет ни одного опроса</div>
                            )}
                        </div>
                    )}
                </Box>
            </div>
        </div>
    );
}
