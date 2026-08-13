import { configureStore } from '@reduxjs/toolkit';
import surveySlice from '@/entities/Survey/Survey.slice';
import errorSlice from '@/entities/Error/Error.slice';
import accountSlice from '@/entities/Account/Account.slice';
import notificationScheduleSlice from '@/entities/NotificationSchedule/NotificationSchedule.slice';
import templateSlice from './Template/Template.slice';
import pagesSlice from './Pages/Pages.slice';
export const store = configureStore({
    reducer: {
        survey: surveySlice,
        error: errorSlice,
        account: accountSlice,
        notificationSchedule: notificationScheduleSlice,
        template: templateSlice,
        pages: pagesSlice,
    },
});

export type TRootState = ReturnType<typeof store.getState>;
export type TAppDispatch = typeof store.dispatch;
