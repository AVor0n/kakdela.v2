export type ScheduleType = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CUSTOM';

export type NotificationSchedule = {
    id: string;
    name: string;
    surveyId: string;
    scheduleType: ScheduleType;
    daysOfWeek: number;
    dayOfMonth: number;
    cronExpression: string;
    executionTime: string;
    targetTimezone: string;
    isActive: boolean;
};
