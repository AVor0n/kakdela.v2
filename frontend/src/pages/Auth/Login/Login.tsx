import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '@/api/auth';
import { routes } from '@/app/routes';
import { AuthCard } from '@/pages/Auth/components/AuthCard';
import { AuthPageLayout } from '@/pages/Auth/components/AuthPageLayout';
import { LoginForm } from '@/pages/Auth/Login/LoginForm';
import { validateLogin, validatePassword } from '@/pages/Auth/validation';

type LoginFormValues = {
    login: string;
    password: string;
};

type LoginFormTouched = Record<keyof LoginFormValues, boolean>;
type LoginFormErrors = Record<keyof LoginFormValues, string>;

const initialValues: LoginFormValues = {
    login: '',
    password: '',
};

const initialTouched: LoginFormTouched = {
    login: false,
    password: false,
};

const initialErrors: LoginFormErrors = {
    login: '',
    password: '',
};

export function Login() {
    const navigate = useNavigate();
    const [values, setValues] = useState<LoginFormValues>(initialValues);
    const [touched, setTouched] = useState<LoginFormTouched>(initialTouched);
    const [errors, setErrors] = useState<LoginFormErrors>(initialErrors);
    const [formError, setFormError] = useState('');

    const validateField = (field: keyof LoginFormValues, value: string): string => {
        if (field === 'login') {
            return validateLogin(value);
        }

        return validatePassword(value);
    };

    const handleFieldChange = (field: keyof LoginFormValues, value: string) => {
        setFormError('');
        setValues((currentValues) => ({
            ...currentValues,
            [field]: value,
        }));

        if (touched[field]) {
            setErrors((currentErrors) => ({
                ...currentErrors,
                [field]: validateField(field, value),
            }));
        }
    };

    const handleFieldBlur = (field: keyof LoginFormValues) => {
        setTouched((currentTouched) => ({
            ...currentTouched,
            [field]: true,
        }));

        setErrors((currentErrors) => ({
            ...currentErrors,
            [field]: validateField(field, values[field]),
        }));
    };

    const handleSubmit = async () => {
        const nextErrors: LoginFormErrors = {
            login: validateLogin(values.login),
            password: validatePassword(values.password),
        };

        setTouched({
            login: true,
            password: true,
        });
        setErrors(nextErrors);

        if (nextErrors.login || nextErrors.password) {
            return;
        }

        try {
            setFormError('');
            const { accessToken } = await login(values);

            localStorage.setItem('accessToken', accessToken);
            // TODO: редирект после успешного логина
        } catch (error) {
            if (axios.isAxiosError(error) && (error.response?.status === 401 || error.response?.status === 403)) {
                setFormError('Неверный логин или пароль');
                return;
            }

            setFormError('Не удалось выполнить вход. Попробуйте позже');
        }
    };

    const handleRegisterClick = () => {
        navigate(routes.register());
    };

    return (
        <AuthPageLayout>
            <AuthCard title="Авторизация">
                <LoginForm
                    values={values}
                    errors={errors}
                    formError={formError}
                    onSubmit={handleSubmit}
                    onRegisterClick={handleRegisterClick}
                    onLoginChange={(value) => handleFieldChange('login', value)}
                    onPasswordChange={(value) => handleFieldChange('password', value)}
                    onLoginBlur={() => handleFieldBlur('login')}
                    onPasswordBlur={() => handleFieldBlur('password')}
                />
            </AuthCard>
        </AuthPageLayout>
    );
}
