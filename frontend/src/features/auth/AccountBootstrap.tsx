import { getAccountDetails } from '@/api/account';
import { clearAccount, setAccount, setLoading } from '@/entities/Account/Account.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useEffect, type ReactNode } from 'react';

interface Props {
    children: ReactNode;
}

export function AccountBootstrap({ children }: Props) {
    const { account } = useAppSelector((state) => state.account);
    const dispatch = useAppDispatch();
    useEffect(() => {
        if (account) {
            dispatch(setLoading(false));
            return;
        }
        dispatch(setLoading(true));
        getAccountDetails()
            .then((data) => {
                dispatch(setAccount(data));
            })
            .catch(() => {
                dispatch(clearAccount());
            })
            .finally(() => {
                dispatch(setLoading(false));
            });
    }, []);
    return children;
}
