import { Button, Checkbox, UncontrolledDateTimeInput } from '@hh.ru/magritte-ui';
import { useEffect, useState } from 'react';
import style from './Settings.module.css';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useDebounce } from '@/hooks/useDebounce';
import { deleteSurvey, updateSurvey } from '@/api/survey';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { ErrorBlock } from '../ErrorBlock/ErrorBlock';
import type { Error } from '@/shared/types/Error.type';
import { useNavigate } from 'react-router-dom';
import { routePatterns } from '@/app/routes';

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
    const [error, setError] = useState<Error | null>(null);
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
                        setError(err.response.data);
                    }
                });

            updateSurvey(selectedSurvey.id, { isLimitedToOneResponse: isLimitedToOneResponse })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((err) => {
                    if (err.response) {
                        setError(err.response.data);
                    }
                });

            updateSurvey(selectedSurvey.id, { doNotify })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((err) => {
                    if (err.response) {
                        setError(err.response.data);
                    }
                });
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
                        setError(err.response.data);
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
                        setError(err.response.data);
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
                        setError(err.response.data);
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
                        setError(err.response.data);
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
                    setError(err.response.data);
                }
            });
    };

    return (
        <section className={style.container}>
            {error && <ErrorBlock error={error} setError={setError} />}
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
                    <span>Отправлять сообщение о необходимости прохождения опроса</span>
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
