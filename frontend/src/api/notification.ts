import type { NotificationSchedule } from '@/shared/types/Notification.type';
import { apiClient } from './client';

type NotificationScheduleResponse = NotificationSchedule[];

type NotificationScheduleRequest = Pick<
    NotificationSchedule,
    | 'name'
    | 'scheduleType'
    | 'daysOfWeek'
    | 'dayOfMonth'
    | 'cronExpression'
    | 'executionTime'
    | 'targetTimezone'
    | 'isActive'
>;

type CreateNotificationScheduleRequest = Pick<
    NotificationScheduleRequest,
    'name' | 'scheduleType' | 'executionTime' | 'targetTimezone'
>;

type UpdateNotificationScheduleRequest = Partial<NotificationScheduleRequest>;

export async function getNotificationBySurveyId(surveyId: string): Promise<NotificationScheduleResponse> {
    const { data } = await apiClient.get<NotificationScheduleResponse>(`/api/surveys/${surveyId}/notifications`);

    return data;
}

export async function createNotificationSchedule(
    surveyId: string,
    request: CreateNotificationScheduleRequest,
): Promise<NotificationSchedule> {
    const { data } = await apiClient.post<NotificationSchedule>(`/api/surveys/${surveyId}/notifications`, {
        type: request.scheduleType,
        ...request,
    });

    return data;
}

export async function updateNotificationSchedule(
    notificationId: string,
    request: UpdateNotificationScheduleRequest,
): Promise<NotificationSchedule> {
    const { data } = await apiClient.patch<NotificationSchedule>(`/api/notifications/${notificationId}`, {
        type: request.scheduleType,
        ...request,
    });

    return data;
}

export async function deleteNotificationSchedule(notificationId: string): Promise<void> {
    await apiClient.delete(`/api/notifications/${notificationId}`);
}
