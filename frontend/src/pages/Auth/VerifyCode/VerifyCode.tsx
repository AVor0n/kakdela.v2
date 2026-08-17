import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { AuthPageLayout } from '../components/AuthPageLayout';
import { AuthCard } from '../components/AuthCard';
import { VerifyCodeForm } from './VerifyCodeForm';
import { routes } from '@/app/routes';
import axios from 'axios';
import { useState } from 'react';
import { validateResetCode } from '../validation';
import { verifyResetCode } from '@/api/auth';

export function VerifyCode() {
    const { email } = useParams();
    const navigate = useNavigate();
    const [code, setCode] = useState('');
    const [codeError, setCodeError] = useState('');
    const [formError, setFormError] = useState('');
    if (!email) {
        return <Navigate to={routes.root()} />;
    }
    const handleCodeChange = (value: string) => {
        setFormError('');
        setCode(value);
    };

    const handleCodeBlur = () => {
        setCodeError(validateResetCode(code));
    };

    const handleSubmit = async () => {
        const error = validateResetCode(code);

        setCodeError(error);

        if (error) {
            return;
        }

        try {
            setFormError('');
            await verifyResetCode(email!, code);
            navigate(routes.resetPassword(email!, code));
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 404) {
                setCodeError('Ошибка отправки кода попробуйте позже');
                return;
            } else if (axios.isAxiosError(error) && error.response?.status === 429) {
                setCodeError(`Слишком много попыток. ${error.response?.data.message}`);
                return;
            }

            setFormError('Неверный код');
        }
    };

    return (
        <AuthPageLayout>
            <AuthCard title='Код подтверждения'>
                <VerifyCodeForm
                    email={email}
                    code={code}
                    onSubmit={handleSubmit}
                    codeError={codeError}
                    formError={formError}
                    onCodeChange={handleCodeChange}
                    onCodeBlur={handleCodeBlur}
                />
            </AuthCard>
        </AuthPageLayout>
    );
}
