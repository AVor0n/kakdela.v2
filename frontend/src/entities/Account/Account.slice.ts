import type { Account } from '@/shared/types/Account.type';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export interface IAccountState {
    account: Account | null;
    loading: boolean;
}

const initialState: IAccountState = {
    account: null,
    loading: false,
};

export const accountSlice = createSlice({
    name: 'account',
    initialState,
    reducers: {
        setAccount(state, action: PayloadAction<Account>) {
            state.account = action.payload;
        },
        clearAccount(state) {
            state.account = null;
        },
        setLoading(state, action: PayloadAction<boolean>) {
            state.loading = action.payload;
        },
    },
});

export const { setAccount, clearAccount, setLoading } = accountSlice.actions;

export default accountSlice.reducer;
