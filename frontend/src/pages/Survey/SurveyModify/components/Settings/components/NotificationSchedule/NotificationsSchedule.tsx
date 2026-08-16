import { createNotificationSchedule, getNotificationBySurveyId } from '@/api/notification';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { Button, Flex, Text } from '@hh.ru/magritte-ui';
import { useEffect } from 'react';
import { NotificationItem } from './components/NotificationItem';
import { useAppSelector } from '@/hooks/useAppSelector';
import {
    addNotificationSchedule,
    setNotificationsSchedule,
} from '@/entities/NotificationSchedule/NotificationSchedule.slice';
import style from './NotificationsSchedule.module.css';
interface Props {
    surveyId: string;
}

export function NotificationsSchedule({ surveyId }: Props) {
    const { notificationsSchedule } = useAppSelector((state) => state.notificationSchedule);
    const dispatch = useAppDispatch();
    useEffect(() => {
        getNotificationBySurveyId(surveyId)
            .then((data) => {
                dispatch(setNotificationsSchedule(data));
            })
            .catch(() =>
                dispatch(setErrorMessage({ message: 'Не удалось получить настройки переодических уведомлений' })),
            );
    }, [surveyId]);

    const createNotificationScheduleHandler = () => {
        if (notificationsSchedule.length + 1 > 3) {
            dispatch(
                setErrorMessage({ message: 'Невозможно создать больше 3 настроек для переодичесуих уведомлений' }),
            );
            return;
        }

        const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
        const currentHour = new Date().getHours();
        createNotificationSchedule(surveyId, {
            name: 'test',
            scheduleType: 'DAILY',
            executionTime: `${currentHour}:00`,
            targetTimezone: timeZone,
        })
            .then((data) => {
                dispatch(addNotificationSchedule(data));
            })
            .catch(() =>
                dispatch(setErrorMessage({ message: 'Не удалось создать настройки переодических уведомлений' })),
            );
    };

    return (
        <div className={style.container}>
            {notificationsSchedule.length === 0 ? (
                <Button type='button' mode='secondary' style='accent' onClick={createNotificationScheduleHandler}>
                    Добавить отправку переодических уведомлений
                </Button>
            ) : (
                <>
                    <div className={style.header}>
                        <Text typography='title-4-semibold'>Настройка напоминаний о прохождении</Text>
                    </div>
                    <div className={style.content}>
                        {notificationsSchedule.map((notification) => {
                            return <NotificationItem key={notification.id} notificationSchedule={notification} />;
                        })}
                    </div>
                    {notificationsSchedule.length < 3 && (
                        <Flex gap={10} direction={'column'}>
                            <Button
                                type='button'
                                size='small'
                                mode='secondary'
                                style='accent'
                                onClick={createNotificationScheduleHandler}
                                icon={<img src='/add.svg' alt='+' />}
                            >
                                Добавить{' '}
                            </Button>
                        </Flex>
                    )}
                </>
            )}
        </div>
    );
}
