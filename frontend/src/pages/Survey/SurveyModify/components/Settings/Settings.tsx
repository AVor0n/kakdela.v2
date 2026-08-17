import { useEffect, useRef, useState } from 'react';
import { useAppSelector } from '@/hooks/useAppSelector';
import { deleteSurvey, updateSurvey } from '@/api/survey';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { useNavigate } from 'react-router-dom';
import { routePatterns } from '@/app/routes';
import { SubscribersInput } from './components/Subscribers/SubscribersInput';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { Permissions } from './components/Permissions/Permissions';
import style from './Settings.module.css';
import { Button, Checkbox, DateTimeInput } from '@hh.ru/magritte-ui';
import classNames from 'classnames';
import { NotificationsSchedule } from './components/NotificationSchedule/NotificationsSchedule';
import { createTemplateFromSurvey } from '@/api/template';

function convertDateFromISO(isoStr: string): string {
    if (!isoStr) return '';
    const date = new Date(isoStr);

    const datePart = date.toLocaleDateString('ru-RU');
    const timePart = date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });

    return `${datePart}, ${timePart}`;
}

export function Settings() {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const { account } = useAppSelector((state) => state.account);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const isAuthor = account?.id === selectedSurvey?.author.id;
    const [isTemplateCreated, setIsTemplateCreated] = useState(false);

    const [isAuthorizedOnly, setIsAuthorizedOnly] = useState<boolean>(selectedSurvey?.isAuthorizedOnly ?? false);
    const [isLimitedToOneResponse, setIsLimitedToOneResponse] = useState<boolean>(
        selectedSurvey?.isLimitedToOneResponse ?? false,
    );
    const [doNotify, setDoNotify] = useState<boolean>(selectedSurvey?.doNotify ?? false);
    const [expireAt, setExpireAt] = useState<string | null>(() => {
        if (selectedSurvey && selectedSurvey.expireAt) {
            return convertDateFromISO(selectedSurvey.expireAt);
        }

        return null;
    });

    const skipSaveOnUnmountRef = useRef<boolean>(false);
    const expireAtRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (!selectedSurvey) return;
        setExpireAt(convertDateFromISO(selectedSurvey.expireAt ?? ''));
    }, [selectedSurvey?.expireAt]);

    useEffect(() => {
        if (!selectedSurvey) return;
        setIsAuthorizedOnly(selectedSurvey.isAuthorizedOnly);
    }, [selectedSurvey?.isAuthorizedOnly]);

    useEffect(() => {
        if (!selectedSurvey) return;
        setIsLimitedToOneResponse(selectedSurvey.isLimitedToOneResponse);
    }, [selectedSurvey?.isLimitedToOneResponse]);

    useEffect(() => {
        if (!selectedSurvey) return;
        setDoNotify(selectedSurvey.doNotify);
    }, [selectedSurvey?.doNotify]);

    const updateIsAuthorizedOnlyHandler = (newValue: boolean) => {
        if (!selectedSurvey) return;
        setIsAuthorizedOnly(newValue);
        if (newValue !== selectedSurvey.isAuthorizedOnly) {
            updateSurvey(selectedSurvey.id, { isAuthorizedOnly: newValue })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(
                            setErrorMessage({
                                message:
                                    'Не удалось изменить настройку "Запретить анонимное прохождение"',
                            }),
                        );
                    }
                });
        }
    };

    const updateIsLimitedToOneResponseHandler = (newValue: boolean) => {
        if (!selectedSurvey) return;
        setIsLimitedToOneResponse(newValue);
        if (newValue !== selectedSurvey.isLimitedToOneResponse) {
            updateSurvey(selectedSurvey.id, { isLimitedToOneResponse: newValue })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(
                            setErrorMessage({
                                message: 'Не удалось изменить настройку "Запретить проходить более одного раза"',
                            }),
                        );
                    }
                });
        }
    };

    const updateDoNotifyHandler = (newValue: boolean) => {
        if (!selectedSurvey) return;
        setDoNotify(newValue);
        if (newValue !== selectedSurvey.doNotify) {
            updateSurvey(selectedSurvey.id, { doNotify: newValue })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(
                            setErrorMessage({
                                message: 'Не удалось изменить настройку "Дата и время окончания приёма ответов"',
                            }),
                        );
                    }
                });
        }
    };

    const changeExpireAt = () => {
        if (!selectedSurvey) return;
        if (expireAt !== selectedSurvey.expireAt) {
            let isoString = '';
            const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
            if (expireAt) {
                const [datePart, timePart] = expireAt.split(',').map((part) => part.trim());
                const [day, month, year] = datePart.split('.').map(Number);
                if (!day || !month || !year) {
                    return;
                }
                const [hours, minutes] = timePart ? timePart.split(':').map(Number) : [10, 0];
                const pad = (value: number) => String(value).padStart(2, '0');
                isoString = `${year}-${pad(month)}-${pad(day)}T${pad(hours || 0)}:${pad(minutes || 0)}:00`;
            }
            updateSurvey(selectedSurvey.id, {
                expireAtAtTargetTimezone: isoString,
                targetTimezone: timeZone,
            })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(
                            setErrorMessage({
                                message: 'Не удалось изменить настройку "Дата и время окончания приёма ответов"',
                            }),
                        );
                    }
                });
        }
    };

    const clearExpireAtHandler = () => {
        if (!selectedSurvey) return;
        updateSurvey(selectedSurvey.id, {
            expireAtAtTargetTimezone: null,
        })
            .then((data) => {
                dispatch(setSelectedSurvey({ survey: data }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(
                        setErrorMessage({
                            message: 'Не удалось изменить настройку "Присылать уведомления о новых ответах"',
                        }),
                    );
                }
            });
    };

    useEffect(() => {
        return () => {
            if (skipSaveOnUnmountRef) return;
            if (!selectedSurvey) return;
            updateIsAuthorizedOnlyHandler(isAuthorizedOnly);
            updateIsLimitedToOneResponseHandler(isLimitedToOneResponse);
            updateDoNotifyHandler(doNotify);
        };
    }, []);

    if (!selectedSurvey) return <div>Loading...</div>;

    const resetSettings = () => {
        updateIsAuthorizedOnlyHandler(true);
        updateIsLimitedToOneResponseHandler(false);
        updateDoNotifyHandler(false);
    };

    const deleteSurveyHandler = () => {
        deleteSurvey(selectedSurvey.id)
            .then(() => {
                skipSaveOnUnmountRef.current = true;
                dispatch(setSelectedSurvey({ survey: null }));
                navigate(routePatterns.surveys);
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(
                        setErrorMessage({
                            message: 'Не удалось удалить опрос',
                        }),
                    );
                }
            });
    };

    const makeTemplateHandler = () => {
        if (!selectedSurvey) return;
        createTemplateFromSurvey(selectedSurvey.id)
            .then(() => {
                setIsTemplateCreated(true);
            })
            .catch(() => dispatch(setErrorMessage({ message: 'Не удалось создать шаблон из этого опроса' })));
    };

    useEffect(() => {
        if (!isTemplateCreated) return;
        const handler = setTimeout(() => {
            setIsTemplateCreated(false);
        }, 2000);

        return () => {
            clearTimeout(handler);
        };
    }, [isTemplateCreated]);

    return (
        <section className={style.container}>
            <div className={style.content}>
                <div className={style.option}>
                    <Checkbox
                        checked={isAuthorizedOnly}
                        onChange={() => {
                            updateIsAuthorizedOnlyHandler(!isAuthorizedOnly);
                        }}
                    />
                    <span>Запретить анонимное прохождение</span>
                </div>
                <div className={style.option}>
                    <Checkbox
                        checked={isLimitedToOneResponse}
                        onChange={() => {
                            updateIsLimitedToOneResponseHandler(!isLimitedToOneResponse);
                        }}
                    />
                    <span>Запретить проходить более одного раза</span>
                </div>

                <div className={style.option}>
                    <Checkbox
                        checked={doNotify}
                        onChange={() => {
                            updateDoNotifyHandler(!doNotify);
                        }}
                    />
                    <span>Присылать уведомления о новых ответах</span>
                </div>

                <div className={classNames(style.option, style.dateOption)}>
                    <DateTimeInput
                        size='large'
                        value={expireAt ?? ''}
                        onChange={(e) => setExpireAt(e)}
                        elevatePlaceholder
                        placeholder='Дата и время окончания приёма ответов'
                        dateMask='dd.mm.yyyy'
                        timeMask
                        onBlur={() => {
                            changeExpireAt();
                        }}
                        ref={expireAtRef}
                    />
                    {expireAt && (
                        <img
                            src='/X.svg'
                            alt='X'
                            onClick={() => {
                                setExpireAt(null);
                                clearExpireAtHandler();
                            }}
                            className={style.clear}
                        />
                    )}
                </div>

                <NotificationsSchedule surveyId={selectedSurvey.id} />

                <SubscribersInput />

                {isAuthor && <Permissions surveyId={selectedSurvey.id} />}

                <div className={style.buttons}>
                    <Button mode='secondary' style='neutral' onClick={resetSettings}>
                        Сбросить настройки
                    </Button>
                    {isAuthor && (
                        <Button mode='secondary' style='negative' onClick={deleteSurveyHandler}>
                            Удалить опрос
                        </Button>
                    )}
                    <Button
                        mode='secondary'
                        style={isTemplateCreated ? 'positive' : 'neutral'}
                        onClick={!isTemplateCreated ? makeTemplateHandler : () => {}}
                    >
                        {!isTemplateCreated ? 'Создать шаблон из этого опроса' : 'Шаблон создан'}
                    </Button>
                </div>
            </div>
        </section>
    );
}
