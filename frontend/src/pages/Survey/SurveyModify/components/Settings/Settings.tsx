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

function convertDateFromISO(isoStr: string): string {
    if (!isoStr) return '';
    const date = new Date(isoStr);

    const result = date.toLocaleDateString('ru-RU');

    return result;
}

export function Settings() {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const { account } = useAppSelector((state) => state.account);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const isAuthor = account?.id === selectedSurvey?.author.id;

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

    // Состояние для отслеживания успешного копирования
    const [isCopied, setIsCopied] = useState(false);

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
                                    'Не удалось изменить настройку "Прохождение только для авторизированных пользователей"',
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
                                message: 'Не удалось изменить настройку "Разрешить проходить опрос только один раз"',
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
                                message: 'Не удалось изменить настройку "Дата окончания прохождения опроса"',
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
                const [day, month, year] = expireAt.split('.').map(Number);
                if (!day || !month || !year) {
                    return;
                }
                isoString = new Date(year, month - 1, day, 10, 0, 0).toISOString();
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
                                message: 'Не удалось изменить настройку "Присылать сообщение о прохождении опроса"',
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
                            message: 'Не удалось изменить настройку "Присылать сообщение о прохождении опроса"',
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
        <section className={style.container}>
            <div className={style.content}>
                <div className={style.option}>
                    <Checkbox
                        checked={isAuthorizedOnly}
                        onChange={() => {
                            updateIsAuthorizedOnlyHandler(!isAuthorizedOnly);
                        }}
                    />
                    <span>Прохождение только для авторизированных пользователей</span>
                </div>
                <div className={style.option}>
                    <Checkbox
                        checked={isLimitedToOneResponse}
                        onChange={() => {
                            updateIsLimitedToOneResponseHandler(!isLimitedToOneResponse);
                        }}
                    />
                    <span>Разрешить проходить опрос только один раз</span>
                </div>

                <div className={style.option}>
                    <Checkbox
                        checked={doNotify}
                        onChange={() => {
                            updateDoNotifyHandler(!doNotify);
                        }}
                    />
                    <span>Присылать сообщение о прохождении опроса</span>
                </div>

                <div className={classNames(style.option, style.dateOption)}>
                    <DateTimeInput
                        size='large'
                        value={expireAt ?? ''}
                        onChange={(e) => setExpireAt(e)}
                        elevatePlaceholder
                        placeholder='Дата окончания прохождения опроса'
                        dateMask='dd.mm.yyyy'
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
                        style='neutral'
                        onClick={() =>
                            handleCopyClick(
                                `https://${window.location.hostname}:${window.location.port}/surveys/${selectedSurvey.id}?responde=true`,
                            )
                        }
                    >
                        {isCopied ? 'Ссылка скопирована' : 'Скопировать ссылку на опрос'}
                    </Button>
                </div>
            </div>
        </section>
    );
}
