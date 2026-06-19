import { Navigate, useLocation } from 'react-router-dom';
import { useEffect, useState, type ReactNode } from 'react';
import { routes } from '@/app/routes';

type RequireAuthProps = {
    children: ReactNode;
};

export function RequireAuth({ children }: RequireAuthProps) {
    const location = useLocation();
    const [accessToken, setAccessToken] = useState<CookieListItem | null | undefined>(undefined);
    useEffect(() => {
        cookieStore.get('accessToken').then(setAccessToken);
    });

    if (accessToken === undefined) {
        return null;
    }

    if (!accessToken) {
        return <Navigate to={routes.login()} state={{ from: location }} replace />;
    }

    return children;
}
