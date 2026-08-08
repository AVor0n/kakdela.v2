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
import { SurveyMobileMenu } from './components/SurveyMobileMenu/SurveyMobileMenu';
import type { SurveyNavigationItem, SurveySection } from './SurveyLayout.types';

type SurveyAccess = {
    surveyId: string;
    role: SurveyRole;
};

function getActiveSection(pathname: string): SurveySection | null {
    if (pathname.includes('/questions')) return 'questions';
    if (pathname.includes('/answers')) return 'answers';
    if (pathname.includes('/settings')) return 'settings';
    return null;
}

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
    const activeSection = getActiveSection(pathname);
    const navigationItems: SurveyNavigationItem[] = [
        {
            section: 'questions',
            label: 'Вопросы',
            path: `${basePath}/questions`,
            disabled: !canEditSurvey,
            disabledTitle: 'Аналитику недоступно редактирование вопросов',
            disabledAriaLabel: 'Вопросы недоступны для аналитика',
        },
        {
            section: 'answers',
            label: 'Ответы',
            path: `${basePath}/answers`,
            disabled: isAccessLoading,
        },
        {
            section: 'settings',
            label: 'Настройки',
            path: `${basePath}/settings`,
            disabled: !canEditSurvey,
            disabledTitle: 'Аналитику недоступны настройки опроса',
            disabledAriaLabel: 'Настройки недоступны для аналитика',
        },
    ];

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
                <div className={style.backLink}>
                    <LinkHH mode='primary' style='accent' href={routes.survey()}>
                        Обратно в меню
                    </LinkHH>
                </div>

                <nav className={style.navbar}>
                    {navigationItems.map((item) => (
                        <Button
                            key={item.section}
                            mode={activeSection === item.section ? 'primary' : 'secondary'}
                            style='accent'
                            Element={Link}
                            to={item.path}
                            disabled={item.disabled}
                            title={item.disabled ? item.disabledTitle : undefined}
                            aria-label={item.disabled ? item.disabledAriaLabel : item.label}
                        >
                            {item.label}
                        </Button>
                    ))}
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

                <div className={style.mobileHeader}>
                    <SurveyMobileMenu
                        surveyId={id}
                        navigationItems={navigationItems}
                        activeSection={activeSection}
                        canEditSurvey={canEditSurvey}
                        isAccessLoading={isAccessLoading}
                        hasSelectedSurvey={Boolean(selectedSurvey)}
                        isPublished={selectedSurvey?.isPublished}
                        onPublish={publishingHandler}
                    />
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
