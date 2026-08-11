import type { NotificationSchedule } from '@/shared/types/Notification.type';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface NotificationScheduleState {
    notificationsSchedule: NotificationSchedule[];
}

const initialState: NotificationScheduleState = {
    notificationsSchedule: [],
};

const notificationScheduleSlice = createSlice({
    name: 'notificationSchedule',
    initialState,
    reducers: {
        setNotificationsSchedule: (state, action: PayloadAction<NotificationSchedule[]>) => {
            state.notificationsSchedule = action.payload;
        },
        addNotificationSchedule: (state, action: PayloadAction<NotificationSchedule>) => {
            state.notificationsSchedule.push(action.payload);
        },
        updateNotificationSchedule: (state, action: PayloadAction<NotificationSchedule>) => {
            state.notificationsSchedule = state.notificationsSchedule.map((notification) =>
                notification.id === action.payload.id ? action.payload : notification,
            );
        },
        deleteNotificationSchedule: (state, action: PayloadAction<string>) => {
            state.notificationsSchedule = state.notificationsSchedule.filter(
                (notification) => notification.id !== action.payload,
            );
        },
    },
});

export const {
    setNotificationsSchedule,
    updateNotificationSchedule,
    deleteNotificationSchedule,
    addNotificationSchedule,
} = notificationScheduleSlice.actions;

export default notificationScheduleSlice.reducer;
