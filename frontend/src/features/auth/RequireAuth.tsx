import { Navigate, useLocation } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAppSelector } from '@/app/hooks';
import { routes } from '@/app/routes';

type RequireAuthProps = {
    children: ReactNode;
};

export function RequireAuth({ children }: RequireAuthProps) {
    const location = useLocation();
    const accessToken = useAppSelector((state) => state.auth.accessToken);

    if (!accessToken) {
        return <Navigate to={routes.login()} state={{ from: location }} replace />;
    }

    return children;
}
