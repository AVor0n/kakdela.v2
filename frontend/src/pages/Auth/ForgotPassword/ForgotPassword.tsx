import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { forgotPassword } from '@/api/auth';
import { routes } from '@/app/routes';
import { AuthCard } from '@/pages/Auth/components/AuthCard';
import { AuthPageLayout } from '@/pages/Auth/components/AuthPageLayout';
import { ForgotPasswordForm } from '@/pages/Auth/ForgotPassword/ForgotPasswordForm';
import { validateEmail } from '@/pages/Auth/validation';

export function ForgotPassword() {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [emailError, setEmailError] = useState('');
    const [formError, setFormError] = useState('');

    const handleEmailChange = (value: string) => {
        setFormError('');
        setEmail(value);
    };

    const handleEmailBlur = () => {
        setEmailError(validateEmail(email));
    };

    const handleSubmit = async () => {
        const error = validateEmail(email);

        setEmailError(error);

        if (error) {
            return;
        }

        try {
            setFormError('');
            await forgotPassword(email);
            navigate(routes.verifyCode(email));
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 404) {
                setEmailError('Аккаунт с такой почтой не найден');
                return;
            } else if (axios.isAxiosError(error) && error.response?.status === 429) {
                setEmailError(`Слишком много попыток. ${error.response?.data.message}`);
                return;
            }

            setFormError('Не удалось отправить письмо. Попробуйте позже');
        }
    };

    return (
        <AuthPageLayout>
            <AuthCard title='Сброс пароля'>
                <ForgotPasswordForm
                    email={email}
                    emailError={emailError}
                    formError={formError}
                    onSubmit={() => void handleSubmit()}
                    onEmailChange={(value) => handleEmailChange(value)}
                    onEmailBlur={handleEmailBlur}
                />
            </AuthCard>
        </AuthPageLayout>
    );
}
