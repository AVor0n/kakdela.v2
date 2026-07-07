import { Button, Checkbox, UncontrolledDateTimeInput } from '@hh.ru/magritte-ui';
import { useEffect, useState } from 'react';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useDebounce } from '@/hooks/useDebounce';
import { deleteSurvey, updateSurvey } from '@/api/survey';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { useNavigate } from 'react-router-dom';
import { routePatterns } from '@/app/routes';
import { SubscribersInput } from './Subscribers/SubscribersInput';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './Settings.module.css';

function convertDateFromISO(isoStr: string): string {
    const date = new Date(isoStr);

    const result = date.toLocaleDateString('ru-RU');

    return result;
}

export function Settings() {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    if (!selectedSurvey) return null;

    const [isAuthorizedOnly, setIsAuthorizedOnly] = useState<boolean>(selectedSurvey.isAuthorizedOnly);
    const [isLimitedToOneResponse, setIsLimitedToOneResponse] = useState<boolean>(
        selectedSurvey.isLimitedToOneResponse,
    );
    const [doNotify, setDoNotify] = useState<boolean>(selectedSurvey.doNotify);
    const [expireAt, setExpireAt] = useState<string | null>(() => {
        if (selectedSurvey.expireAt) {
            return convertDateFromISO(selectedSurvey.expireAt);
        }

        return null;
    });
    const debouncedIsAuthorizedOnly = useDebounce(isAuthorizedOnly, 1000);
    const debouncedIsLimitedToOneResponse = useDebounce(isLimitedToOneResponse, 1000);
    const debouncedDoNotify = useDebounce(doNotify, 1000);
    const debouncedExpireAt = useDebounce(expireAt, 1000);

    useEffect(() => {
        return () => {
            updateSurvey(selectedSurvey.id, { isAuthorizedOnly: isAuthorizedOnly })
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

            updateSurvey(selectedSurvey.id, { isLimitedToOneResponse: isLimitedToOneResponse })
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

            updateSurvey(selectedSurvey.id, { doNotify })
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
            if (debouncedExpireAt) {
                const [day, month, year] = debouncedExpireAt.split('.').map(Number);
                if (!day || !month || !year) {
                    return;
                }
                const isoString = new Date(year, month - 1, day, 10, 0, 0).toISOString();
                const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

                updateSurvey(selectedSurvey.id, { expireAtAtTargetTimezone: isoString, targetTimezone: timeZone })
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
    }, []);

    useEffect(() => {
        if (debouncedIsAuthorizedOnly !== selectedSurvey.isAuthorizedOnly) {
            updateSurvey(selectedSurvey.id, { isAuthorizedOnly: isAuthorizedOnly })
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
    }, [debouncedIsAuthorizedOnly]);

    useEffect(() => {
        if (debouncedIsLimitedToOneResponse !== selectedSurvey.isLimitedToOneResponse) {
            updateSurvey(selectedSurvey.id, { isLimitedToOneResponse: isLimitedToOneResponse })
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
    }, [debouncedIsLimitedToOneResponse]);

    useEffect(() => {
        if (debouncedDoNotify !== selectedSurvey.doNotify) {
            updateSurvey(selectedSurvey.id, { doNotify })
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
    }, [debouncedDoNotify]);

    useEffect(() => {
        if (debouncedExpireAt && debouncedExpireAt !== selectedSurvey.expireAt) {
            const [day, month, year] = debouncedExpireAt.split('.').map(Number);
            if (!day || !month || !year) {
                return;
            }
            const isoString = new Date(year, month - 1, day, 10, 0, 0).toISOString();
            const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

            updateSurvey(selectedSurvey.id, { expireAtAtTargetTimezone: isoString, targetTimezone: timeZone })
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
    }, [debouncedExpireAt]);

    const resetSettings = () => {
        setIsAuthorizedOnly(true);
        setIsLimitedToOneResponse(false);
        setDoNotify(false);
    };

    const deleteSurveyHandler = () => {
        deleteSurvey(selectedSurvey.id)
            .then(() => navigate(routePatterns.surveys))
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

    return (
        <section className={style.container}>
            <div className={style.content}>
                <p className={style.title}>Настройки</p>
                <div className={style.option}>
                    <Checkbox checked={isAuthorizedOnly} onChange={() => setIsAuthorizedOnly(!isAuthorizedOnly)} />
                    <span>Прохождение только для авторизированных пользователей</span>
                </div>

                <div className={style.option}>
                    <Checkbox
                        checked={isLimitedToOneResponse}
                        onChange={() => setIsLimitedToOneResponse(!isLimitedToOneResponse)}
                    />
                    <span>Разрешить проходить опрос только один раз</span>
                </div>

                <div className={style.option}>
                    <Checkbox checked={doNotify} onChange={() => setDoNotify(!doNotify)} />
                    <span>Присылать сообщение о прохождении опроса</span>
                </div>

                <div className={style.option}>
                    <UncontrolledDateTimeInput
                        size='large'
                        value={expireAt ? expireAt : ''}
                        onChange={(e) => setExpireAt(e)}
                        elevatePlaceholder
                        placeholder='Дата окончания опроса'
                        dateMask='dd.mm.yyyy'
                    />
                </div>

                <SubscribersInput />

                <div className={style.buttons}>
                    <Button mode='secondary' style='neutral' onClick={resetSettings}>
                        Сбросить настройки
                    </Button>
                    <Button mode='secondary' style='negative' onClick={deleteSurveyHandler}>
                        Удалить опрос
                    </Button>
                </div>
            </div>
        </section>
    );
}
