import { matchPath } from 'react-router-dom';
import { getAccountDetails } from '@/api/account';
import { clearAccount, setAccount, setLoading } from '@/entities/Account/Account.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useEffect, type ReactNode } from 'react';
import { routePatterns } from '@/app/routes';

interface Props {
    children: ReactNode;
}

const ANONYMOUS_ALLOWED_PATTERNS = [routePatterns.root, routePatterns.surveysView];

export function AccountBootstrap({ children }: Props) {
    const { account } = useAppSelector((state) => state.account);
    const dispatch = useAppDispatch();

    useEffect(() => {
        if (account) {
            dispatch(setLoading(false));
            return;
        }

        const isAnonymousAllowedPage = ANONYMOUS_ALLOWED_PATTERNS.some((pattern) =>
            matchPath(pattern, window.location.pathname),
        );

        if (!isAnonymousAllowedPage) {
            dispatch(setLoading(true));
        }

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
    }, [account]);
    return children;
}
