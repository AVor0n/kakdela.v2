import { Link, Outlet, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { routePatterns, routes } from '@/app/routes';
import { Link as LinkHH, Button } from '@hh.ru/magritte-ui';
import style from './SurveyLayout.module.css';
import { getMySurveys, getSurveyForEditById, updateSurvey } from '@/api/survey';
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
import { getTemplateById, saveTemplate, updateTemplate } from '@/api/template';
import { addTemplate, setSelectedTemplate } from '@/entities/Template/Template.slice';
import { setPages } from '@/entities/Pages/Pages.slice';
import { validateActiveSurveyConditions } from '@/shared/utils/conditions';
import { EyeOutlinedSize24, LinkOutlinedSize24 } from '@hh.ru/magritte-ui/icon';

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
    const { selectedTemplate } = useAppSelector((state) => state.template);
    const { account } = useAppSelector((state) => state.account);
    const { pages } = useAppSelector((state) => state.pages);
    const [searchParams] = useSearchParams();
    const [isLoading, setIsLoading] = useState<boolean>(true);

    const [isCopied, setIsCopied] = useState(false);
    const [surveyAccess, setSurveyAccess] = useState<SurveyAccess | null>(null);
    const [isSaveTemplate, setIsSaveTemplate] = useState<boolean>(false);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const userRole = id && surveyAccess?.surveyId === id ? surveyAccess.role : null;
    const isAccessLoading = Boolean(id) && userRole === null;
    const isTemplate = searchParams.get('template') === 'true';
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
        if (isTemplate) {
            getTemplateById(id)
                .then((data) => {
                    dispatch(setSelectedTemplate({ template: data }));
                    dispatch(setPages({ pages: data.pages }));
                    setIsLoading(false);
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Такого шаблона не существует' }));
                        navigate(routePatterns.notFound);
                    }
                });
            return;
        } else {
            getSurveyForEditById(id)
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                    dispatch(setPages({ pages: data.pages }));
                    setIsLoading(false);
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Такого опроса не существует' }));
                        navigate(routePatterns.notFound);
                    }
                });
            return;
        }

        navigate(routePatterns.notFound);
    }, [dispatch, id]);

    useEffect(() => {
        if (!id) return;

        let isActive = true;
        if (!isTemplate)
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
        if (!id || !selectedSurvey) return;

        if (!selectedSurvey.isPublished && pages.length === 0) {
            dispatch(setErrorMessage({ message: 'Добавьте хотя бы одну страницу перед публикацией опроса' }));
            return;
        }

        if (!selectedSurvey.isPublished) {
            const conditionIssues = validateActiveSurveyConditions(pages);
            if (conditionIssues.length > 0) {
                const pageNumbers = [...new Set(conditionIssues.map(({ pageSerialNumber }) => pageSerialNumber))].sort(
                    (firstPageNumber, secondPageNumber) => firstPageNumber - secondPageNumber,
                );
                const pageLabel =
                    pageNumbers.length === 1 ? `странице ${pageNumbers[0]}` : `страницах ${pageNumbers.join(', ')}`;
                dispatch(
                    setErrorMessage({
                        message: `Проверьте активные правила на ${pageLabel} перед публикацией опроса`,
                    }),
                );
                return;
            }
        }

        updateSurvey(id, { isPublished: !selectedSurvey.isPublished })
            .then((data) => {
                dispatch(setSelectedSurvey({ survey: data }));
            })
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось изменить статус публикации опроса' }));
            });
    };

    const publishingTemplateHandler = () => {
        if (id && selectedTemplate) {
            updateTemplate(id, { isPublished: !selectedTemplate.published })
                .then((data) => {
                    dispatch(setSelectedTemplate({ template: data }));
                })
                .catch(() => dispatch(setErrorMessage({ message: 'Не удалось опубликовать шаблон' })));
        }
    };

    useEffect(() => {
        if (!isSaveTemplate) return;
        const handle = setTimeout(() => {
            setIsSaveTemplate(false);
        }, 2000);

        return () => {
            clearTimeout(handle);
        };
    }, [isSaveTemplate]);

    const saveTemplateHandler = () => {
        if (id && selectedTemplate)
            saveTemplate(selectedTemplate.id)
                .then((data) => {
                    dispatch(addTemplate(data));
                    setIsSaveTemplate(true);
                })
                .catch(() => dispatch(setErrorMessage({ message: 'Не удалось сохранить шаблон' })));
    };

    const handleCopyClick = async (valueForCopy: string) => {
        try {
            // Копируем значение в буфер обмена
            await navigator.clipboard.writeText(valueForCopy);
            setIsCopied(true);

            // Возвращаем исходный текст кнопки через 2 секунды
            setTimeout(() => {
                setIsCopied(false);
            }, 2000);
        } catch (err) {
            dispatch(setErrorMessage({ message: 'Ошибка при копировании: ' + err }));
        }
    };

    return (
        <>
            <header className={style.header}>
                <div className={style.backLink}>
                    <LinkHH mode='primary' style='accent' href={routes.survey()}>
                        Обратно в меню
                    </LinkHH>
                </div>

                {!isTemplate && (
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
                )}
                <div className={style.actions}>
                    <div className={style.copyLink}>
                        <Button
                            mode='secondary'
                            style={isCopied ? 'positive' : 'neutral'}
                            icon={<LinkOutlinedSize24 />}
                            hideLabel
                            aria-label={isCopied ? 'Ссылка скопирована' : 'Скопировать ссылку на опрос'}
                            title={isCopied ? 'Ссылка скопирована' : 'Скопировать ссылку на опрос'}
                            onClick={() =>
                                handleCopyClick(
                                    `https://${window.location.hostname}:${window.location.port}/surveys/${selectedSurvey?.id}?responde=true`,
                                )
                            }
                        />
                    </div>
                    {id && (
                        <Button
                            mode='secondary'
                            style='neutral'
                            icon={<EyeOutlinedSize24 />}
                            hideLabel
                            aria-label='Предпросмотр'
                            title='Предпросмотр'
                            Element={Link}
                            to={routes.surveyPreview(id)}
                            disabled={!selectedSurvey || isAccessLoading}
                        />
                    )}

                    {canEditSurvey || account?.id === selectedTemplate?.authorId ? (
                        !isTemplate ? (
                            <Button
                                mode={selectedSurvey?.isPublished ? 'secondary' : 'tertiary'}
                                style={selectedSurvey?.isPublished ? 'positive' : 'accent'}
                                title={selectedSurvey?.isPublished ? 'Снять с публикации' : 'Опубликовать'}
                                onClick={publishingHandler}
                                disabled={!selectedSurvey}
                            >
                                {selectedSurvey?.isPublished ? 'Опубликовано' : 'Опубликовать'}
                            </Button>
                        ) : (
                            <Button
                                mode={selectedTemplate?.published ? 'secondary' : 'tertiary'}
                                style={selectedTemplate?.published ? 'positive' : 'accent'}
                                title={selectedTemplate?.published ? 'Снять с публикации' : 'Опубликовать'}
                                onClick={publishingTemplateHandler}
                                disabled={!selectedTemplate}
                            >
                                {selectedTemplate?.published ? 'Опубликовано' : 'Опубликовать'}
                            </Button>
                        )
                    ) : (
                        <Button
                            mode='primary'
                            style={isSaveTemplate ? 'positive' : 'accent'}
                            onClick={saveTemplateHandler}
                            disabled={!selectedTemplate}
                        >
                            {isSaveTemplate ? 'Шаблон сохранён' : 'Сохранить шаблон'}
                        </Button>
                    )}
                    <AccountDetail />
                </div>

                <div className={style.mobileHeader}>
                    <SurveyMobileMenu
                        surveyId={id}
                        copyClick={() =>
                            handleCopyClick(
                                `https://${window.location.hostname}:${window.location.port}/surveys/${selectedSurvey?.id}?responde=true`,
                            )
                        }
                        isCopied={isCopied}
                        navigationItems={navigationItems}
                        activeSection={activeSection}
                        canEditSurvey={canEditSurvey}
                        isAccessLoading={isAccessLoading}
                        hasSelectedSurvey={Boolean(selectedSurvey)}
                        isPublished={selectedSurvey?.isPublished}
                        onPublish={publishingHandler}
                        isTemplate={isTemplate}
                    />
                    <AccountDetail />
                </div>
            </header>

            {(Boolean(id) &&
                (isLoading || ((selectedSurvey?.id !== id || isAccessLoading) && selectedTemplate?.id !== id))) ||
            isAnalystRestrictedRoute ? (
                <LoadingContent />
            ) : (
                <Outlet />
            )}
        </>
    );
}
