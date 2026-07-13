import { routes } from '@/app/routes';

import { useAppSelector } from '@/hooks/useAppSelector';
import { LoadingContent } from '@/shared/ui/LoadingContent/LoadingContent';
import { type ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';

type RequireAuthProps = {
    children: ReactNode;
};

export function RequireAuth({ children }: RequireAuthProps) {
    const { account, loading } = useAppSelector((state) => state.account);
    const location = useLocation();

    if (loading) return <LoadingContent />;
    if (!account) return <Navigate to={routes.login()} state={{ from: location }} replace />;
    return children;
}
