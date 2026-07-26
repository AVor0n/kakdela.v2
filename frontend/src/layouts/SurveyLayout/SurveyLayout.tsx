import { Link, Outlet, useLocation, useNavigate, useParams } from 'react-router-dom';
import { routePatterns, routes } from '@/app/routes';
import { Link as LinkHH, Button } from '@hh.ru/magritte-ui';
import style from './SurveyLayout.module.css';
import { getMySurveys, getSurveyById, updateSurvey } from '@/api/survey';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { useEffect, useState } from 'react';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { LoadingContent } from '@/shared/ui/LoadingContent/LoadingContent';
import { AccountDetail } from '@/shared/ui/AccountDetail/AccountDetail';
import type { SurveyRole } from '@/shared/types/Survey.type';

type SurveyAccess = {
    surveyId: string;
    role: SurveyRole;
};

export function SurveyLayout() {
    const { id } = useParams();
    const basePath = id ? routes.surveyEdit(id) : routes.surveyCreate();
    const { pathname } = useLocation();
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [surveyAccess, setSurveyAccess] = useState<SurveyAccess | null>(null);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const userRole = id && surveyAccess?.surveyId === id ? surveyAccess.role : null;
    const isAccessLoading = Boolean(id) && userRole === null;
    const canEditSurvey = !id || userRole === 'AUTHOR' || userRole === 'EDITOR';
    const isAnalystRestrictedRoute =
        userRole === 'ANALYST' &&
        (pathname.startsWith(`${basePath}/questions`) || pathname.startsWith(`${basePath}/settings`));

    useEffect(() => {
        if (!id) {
            return;
        }

        setIsLoading(true);
        getSurveyById(id)
            .then((data) => {
                dispatch(setSelectedSurvey({ survey: data }));
                setIsLoading(false);
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: 'Такого опроса не существует' }));
                    navigate(routePatterns.notFound);
                }
            });
    }, [dispatch, id]);

    useEffect(() => {
        if (!id) return;

        let isActive = true;

        getMySurveys()
            .then((surveys) => {
                if (!isActive) return;

                const currentSurvey = surveys.find((survey) => survey.id === id);
                if (!currentSurvey) {
                    dispatch(setErrorMessage({ message: 'У вас нет доступа к этому опросу' }));
                    navigate(routes.survey(), { replace: true });
                    return;
                }

                setSurveyAccess({ surveyId: id, role: currentSurvey.userRole });
            })
            .catch(() => {
                if (isActive) {
                    dispatch(setErrorMessage({ message: 'Не удалось проверить права доступа к опросу' }));
                    navigate(routes.survey(), { replace: true });
                }
            });

        return () => {
            isActive = false;
        };
    }, [dispatch, id, navigate]);

    useEffect(() => {
        if (id && isAnalystRestrictedRoute) {
            navigate(routes.surveyAnswers(id), { replace: true });
        }
    }, [id, isAnalystRestrictedRoute, navigate]);

    const publishingHandler = () => {
        if (id && selectedSurvey)
            updateSurvey(id, { isPublished: !selectedSurvey.isPublished }).then((data) => {
                dispatch(setSelectedSurvey({ survey: data }));
            });
    };

    return (
        <>
            <header className={style.header}>
                <LinkHH mode='primary' style='accent' href={routes.survey()}>
                    Обратно в меню
                </LinkHH>

                <nav className={style.navbar}>
                    <Button
                        mode={pathname.includes('/questions') ? 'primary' : 'secondary'}
                        style='accent'
                        Element={Link}
                        to={`${basePath}/questions`}
                        disabled={!canEditSurvey}
                        title={!canEditSurvey ? 'Аналитику недоступно редактирование вопросов' : undefined}
                        aria-label={!canEditSurvey ? 'Вопросы недоступны для аналитика' : 'Вопросы'}
                    >
                        Вопросы
                    </Button>
                    <Button
                        mode={pathname.includes('/answers') ? 'primary' : 'secondary'}
                        style='accent'
                        Element={Link}
                        to={`${basePath}/answers`}
                        disabled={isAccessLoading}
                    >
                        Ответы
                    </Button>
                    <Button
                        mode={pathname.includes('/settings') ? 'primary' : 'secondary'}
                        style='accent'
                        Element={Link}
                        to={`${basePath}/settings`}
                        disabled={!canEditSurvey}
                        title={!canEditSurvey ? 'Аналитику недоступны настройки опроса' : undefined}
                        aria-label={!canEditSurvey ? 'Настройки недоступны для аналитика' : 'Настройки'}
                    >
                        Настройки
                    </Button>
                </nav>
                <div className={style.actions}>
                    {id && (
                        <Button
                            mode='secondary'
                            style='neutral'
                            Element={Link}
                            to={routes.surveyPreview(id)}
                            disabled={!selectedSurvey || isAccessLoading}
                        >
                            Предпросмотр
                        </Button>
                    )}

                    {canEditSurvey && (
                        <Button mode='tertiary' style='accent' onClick={publishingHandler} disabled={!selectedSurvey}>
                            {selectedSurvey?.isPublished ? 'Снять с публикации' : 'Опубликовать'}
                        </Button>
                    )}
                    <AccountDetail />
                </div>
            </header>

            {(Boolean(id) && (isLoading || selectedSurvey?.id !== id || isAccessLoading)) ||
            isAnalystRestrictedRoute ? (
                <LoadingContent />
            ) : (
                <Outlet />
            )}
        </>
    );
}
