import axios from 'axios';
import { Navigate, useSearchParams } from 'react-router-dom';
import { routePatterns } from './routes';
import { useEffect, useState } from 'react';
import { getAccountDetails, ssoConfirm } from '@/api/account';

import { useAppDispatch } from '@/hooks/useAppDispatch';
import { clearAccount, setAccount, setLoading } from '@/entities/Account/Account.slice';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { AuthPageLayout } from '@/pages/Auth/components/AuthPageLayout';
import { AuthCard } from '@/pages/Auth/components/AuthCard';
import { validateLoginPassword } from '@/pages/Auth/validation';
import { HhLinkConfirmForm } from './HhLinkConfirmForm';

type Status = 'processing' | 'awaiting-password' | 'done';

export function CallbackSso() {
    const dispatch = useAppDispatch();
    const [searchParams] = useSearchParams();
    const error = searchParams.get('error');
    const hhLinkRequired = Boolean(searchParams.get('hh_link_required'));

    const [status, setStatus] = useState<Status>(!error && hhLinkRequired ? 'awaiting-password' : 'processing');
    const [password, setPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [formError, setFormError] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const finish = async () => {
        try {
            const data = await getAccountDetails();
            dispatch(setAccount(data));
        } catch {
            dispatch(clearAccount());
        } finally {
            dispatch(setLoading(false));
            setStatus('done');
        }
    };

    useEffect(() => {
        if (status !== 'processing') {
            return;
        }

        if (error) {
            dispatch(setErrorMessage({ message: 'Этот аккаунт hh уже привязан' }));
            dispatch(setLoading(false));
            setStatus('done');
            return;
        }

        finish();
    }, []);

    const handlePasswordSubmit = async () => {
        const nextPasswordError = validateLoginPassword(password);
        setPasswordError(nextPasswordError);

        if (nextPasswordError) {
            return;
        }

        setFormError('');
        setSubmitting(true);

        try {
            await ssoConfirm(password);
        } catch (submitError) {
            if (
                axios.isAxiosError(submitError) &&
                (submitError.response?.status === 401 || submitError.response?.status === 403)
            ) {
                setPasswordError('Неверный пароль');
            } else {
                setFormError('Не удалось подтвердить привязку аккаунта. Попробуйте позже');
            }
            setSubmitting(false);
            return;
        }

        await finish();
        setSubmitting(false);
    };

    if (status === 'awaiting-password') {
        return (
            <AuthPageLayout>
                <AuthCard title='Подтверждение привязки hh'>
                    <HhLinkConfirmForm
                        password={password}
                        passwordError={passwordError}
                        formError={formError}
                        submitting={submitting}
                        onSubmit={handlePasswordSubmit}
                        onPasswordChange={(value) => {
                            setPassword(value);
                            setPasswordError('');
                            setFormError('');
                        }}
                        onPasswordBlur={() => setPasswordError(validateLoginPassword(password))}
                    />
                </AuthCard>
            </AuthPageLayout>
        );
    }

    if (status !== 'done') {
        return null;
    }

    return <Navigate to={routePatterns.root} />;
}
