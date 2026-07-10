import { getAccountDetails } from '@/api/account';
import { routePatterns } from '@/app/routes';

import { clearAccount, setAccount, setLoading } from '@/entities/Account/Account.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { useAppSelector } from '@/hooks/useAppSelector';
import { LoadingContent } from '@/shared/ui/LoadingContent/LoadingContent';
import { useEffect, useState, type ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';

type RequireAuthProps = {
    children: ReactNode;
};

export function RequireAuth({ children }: RequireAuthProps) {
    const { account } = useAppSelector((state) => state.account);
    const [isChecking, setIsChecking] = useState(!account);
    const dispatch = useAppDispatch();
    const location = useLocation();

    useEffect(() => {
        if (account) {
            setIsChecking(false);
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
                setIsChecking(false);
            });
    }, []);

    if (isChecking) return <LoadingContent />;
    if (!account) return <Navigate to={routePatterns.authLogin} state={{ from: location }} replace />;
    return children;
}
