import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export interface IErrorState {
    message: string | null;
}

const initialState: IErrorState = {
    message: null,
};

const errorSlice = createSlice({
    name: 'error',
    initialState,
    reducers: {
        setErrorMessage: (state, action: PayloadAction<{ message: string }>) => {
            const { message } = action.payload;
            state.message = message;
        },
        clearErrorMessage: (state) => {
            state.message = null;
        },
    },
});

export const { setErrorMessage, clearErrorMessage } = errorSlice.actions;
export default errorSlice.reducer;
