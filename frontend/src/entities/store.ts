import { configureStore } from '@reduxjs/toolkit';
import surveySlice from '@/entities/Survey/Survey.slice';
import errorSlice from '@/entities/Error/Error.slice';
export const store = configureStore({
    reducer: {
        survey: surveySlice,
        error: errorSlice,
    },
});

export type TRootState = ReturnType<typeof store.getState>;
export type TAppDispatch = typeof store.dispatch;
