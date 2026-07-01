import type { ReactNode } from 'react';

type RequireAuthProps = {
    children: ReactNode;
};

export function RequireAuth({ children }: RequireAuthProps) {
    return children;
}
