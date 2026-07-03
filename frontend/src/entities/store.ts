import { configureStore } from '@reduxjs/toolkit';
import surveySlice from '@/entities/Survey/Survey.slice';
export const store = configureStore({
    reducer: {
        survey: surveySlice,
    },
});

export type TRootState = ReturnType<typeof store.getState>;
export type TAppDispatch = typeof store.dispatch;
