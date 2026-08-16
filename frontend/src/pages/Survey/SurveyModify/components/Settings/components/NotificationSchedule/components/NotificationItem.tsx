import { type ScheduleType, type NotificationSchedule } from '@/shared/types/Notification.type';
import { Button, createStaticDataProvider, DateTimeInput, Input, Select, Text } from '@hh.ru/magritte-ui';
import style from './NotificationItem.module.css';
import { useState } from 'react';
import { deleteNotificationSchedule, updateNotificationSchedule } from '@/api/notification';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import {
    updateNotificationSchedule as updateNotificationScheduleState,
    deleteNotificationSchedule as deleteNotificationScheduleState,
} from '@/entities/NotificationSchedule/NotificationSchedule.slice';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import classNames from 'classnames';
import {
    DAYS,
    DAYS_VALUE,
    formatHM,
    getDaysFromNumber,
    getScheduleTypeByValue,
    NOTIFICATION_TYPE_SELECT_DATA,
    type Day,
} from './utils';
interface Props {
    notificationSchedule: NotificationSchedule;
}

export function NotificationItem({ notificationSchedule }: Props) {
    const [excutionTime, setExecutionTime] = useState<string>(formatHM(notificationSchedule.executionTime));
    const [scheduleType, setScheduleType] = useState<ScheduleType>(notificationSchedule.scheduleType);
    const [selectedDays, setSelectedDays] = useState<Day[]>([
        ...new Set(getDaysFromNumber(notificationSchedule.daysOfWeek ?? 0)),
    ]);
    const [dayOfMonth, setDayOfMonth] = useState<number>(notificationSchedule.dayOfMonth);
    const [isActive, setIsActive] = useState<boolean>(notificationSchedule.isActive);
    const dispatch = useAppDispatch();
    const updateScheduleType = (type: ScheduleType) => {
        if (type === notificationSchedule.scheduleType) return;
        const currentDate = new Date();
        let currentMonth = null;
        let weekDays = null;
        if (type === 'MONTHLY') currentMonth = currentDate.getDay() + 1;
        if (type === 'WEEKLY') weekDays = 0b00000000;
        setScheduleType(type);
        updateNotificationSchedule(notificationSchedule.id, {
            scheduleType: type,
            dayOfMonth: currentMonth ?? 0,
            daysOfWeek: weekDays ?? 0,
        })
            .then((data) => dispatch(updateNotificationScheduleState(data)))
            .catch(() => dispatch(setErrorMessage({ message: 'Не удалось изменить тип переодических уведомлений' })));
    };

    const updateScheduleExcutionTime = (excutionTime: string) => {
        if (excutionTime === formatHM(notificationSchedule.executionTime)) return;
        updateNotificationSchedule(notificationSchedule.id, { executionTime: excutionTime })
            .then((data) => dispatch(updateNotificationScheduleState(data)))
            .catch(() =>
                dispatch(setErrorMessage({ message: 'Не удалось изменить время отправки переодических уведомлений' })),
            );
    };

    const updateScheduleDayOfWeek = (day: Day) => {
        const dayNumber = DAYS_VALUE[day];
        const result = selectedDays.includes(day)
            ? notificationSchedule.daysOfWeek - dayNumber
            : notificationSchedule.daysOfWeek + dayNumber;
        if (result === notificationSchedule.daysOfWeek) return;
        updateNotificationSchedule(notificationSchedule.id, { daysOfWeek: result })
            .then((data) => {
                dispatch(updateNotificationScheduleState(data));
                setSelectedDays([...new Set(getDaysFromNumber(data.daysOfWeek))]);
            })
            .catch(() =>
                dispatch(setErrorMessage({ message: 'Не удалось изменить дни отправки переодических уведомлений' })),
            );
    };

    const updateScheduleDayOfMonth = (day: number) => {
        if (day === notificationSchedule.dayOfMonth) return;
        updateNotificationSchedule(notificationSchedule.id, { dayOfMonth: day })
            .then((data) => dispatch(updateNotificationScheduleState(data)))
            .catch(() =>
                dispatch(
                    setErrorMessage({ message: 'Не удалось изменить день месяца отправки переодических уведомлений' }),
                ),
            );
    };

    const updateScheduleActiveHandler = (value: boolean) => {
        if (value === notificationSchedule.isActive) return;
        updateNotificationSchedule(notificationSchedule.id, { isActive: value })
            .then((data) => {
                dispatch(updateNotificationScheduleState(data));
                setIsActive(data.isActive);
            })
            .catch(() =>
                dispatch(
                    setErrorMessage({ message: 'Не удалось изменить день месяца отправки переодических уведомлений' }),
                ),
            );
    };

    const removeNotificationScheduleHandler = () => {
        deleteNotificationSchedule(notificationSchedule.id)
            .then(() => dispatch(deleteNotificationScheduleState(notificationSchedule.id)))
            .catch(() =>
                dispatch(setErrorMessage({ message: 'Не удалось удалить отправку переодических уведомлений' })),
            );
    };
    return (
        <div className={style.container}>
            <section className={style.section}>
                <div className={style.content}>
                    <div className={style.timeInput}>
                        <DateTimeInput
                            value={excutionTime}
                            onChange={(value) => setExecutionTime(value)}
                            timeMask={true}
                            onBlur={(e) => updateScheduleExcutionTime(e.target.value)}
                        />
                    </div>
                    <div className={style.typeSelect}>
                        <Select
                            type='label'
                            value={getScheduleTypeByValue(scheduleType)}
                            dataProvider={createStaticDataProvider(NOTIFICATION_TYPE_SELECT_DATA, 'тип')}
                            widthEqualToActivator={false}
                            dropWidth={170}
                            name='notificationType'
                            onChange={(e) => {
                                updateScheduleType(e.value as ScheduleType);
                            }}
                        />
                    </div>
                </div>
                {notificationSchedule.scheduleType === 'MONTHLY' && (
                    <div className={style.dayOfMonthInput}>
                        <Input
                            type='number'
                            size='large'
                            max={31}
                            min={0}
                            value={dayOfMonth ? dayOfMonth.toString() : ''}
                            placeholder='День месяца'
                            elevatePlaceholder
                            onChange={(e) => {
                                if (Number(e) > 31) {
                                    setDayOfMonth(31);
                                } else if (Number(e) < 0) {
                                    setDayOfMonth(0);
                                } else setDayOfMonth(Number(e));
                            }}
                            onBlur={(e) => updateScheduleDayOfMonth(Number(e.target.value))}
                        />
                    </div>
                )}
                {notificationSchedule.scheduleType === 'WEEKLY' && (
                    <section className={style.daysSection}>
                        <Text typography='label-3-regular'>Дни отправки напоминаний</Text>
                        <div className={style.days}>
                            {DAYS.map((day) => (
                                <button
                                    key={day}
                                    onClick={() => updateScheduleDayOfWeek(day)}
                                    className={classNames(style.day, selectedDays.includes(day) && style.active)}
                                >
                                    {day}
                                </button>
                            ))}
                        </div>
                    </section>
                )}
            </section>

            <div className={style.content}>
                <Button
                    mode='secondary'
                    style={isActive ? 'accent' : 'negative'}
                    onClick={() => updateScheduleActiveHandler(!isActive)}
                >
                    {isActive ? 'Активно' : 'Не активно'}
                </Button>
                <Button
                    mode='secondary'
                    style='negative'
                    icon={<img src='/trash.svg' alt='delete' />}
                    onClick={removeNotificationScheduleHandler}
                />
            </div>
        </div>
    );
}
