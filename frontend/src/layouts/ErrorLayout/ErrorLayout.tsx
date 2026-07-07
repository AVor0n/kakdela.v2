import { useAppSelector } from '@/hooks/useAppSelector';
import type { ReactNode } from 'react';
import { ErrorBlock } from './components/ErrorBlock/ErrorBlock';

interface Props {
    children: ReactNode;
}

export function ErrorLayout({ children }: Props) {
    const error = useAppSelector((state) => state.error);
    return (
        <>
            {error && <ErrorBlock error={error} />}
            {children}
        </>
    );
}
