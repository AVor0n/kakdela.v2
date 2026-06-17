import { configureStore } from '@reduxjs/toolkit';
import surveySlice from '@/entities/Survey/Survey.slice';
import { authReducer } from '@/features/auth/authSlice';
export const store = configureStore({
    reducer: {
        survey: surveySlice,
        auth: authReducer,
    },
});

export type TRootState = ReturnType<typeof store.getState>;
export type TAppDispatch = typeof store.dispatch;
