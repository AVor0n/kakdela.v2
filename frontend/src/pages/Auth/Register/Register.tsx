import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { register } from '@/api/auth';
import { routes } from '@/app/routes';
import { AuthCard } from '@/pages/Auth/components/AuthCard';
import { AuthPageLayout } from '@/pages/Auth/components/AuthPageLayout';
import { RegisterForm } from '@/pages/Auth/Register/RegisterForm';
import {
    validateEmail,
    validateLogin,
    validatePassword,
    validatePasswordConfirmation,
} from '@/pages/Auth/validation';

type RegisterFormValues = {
    login: string;
    email: string;
    password: string;
    passwordConfirmation: string;
};

type RegisterFormTouched = Record<keyof RegisterFormValues, boolean>;
type RegisterFormErrors = Record<keyof RegisterFormValues, string>;

type ApiErrorResponse = {
    error?: unknown;
};

const initialValues: RegisterFormValues = {
    login: '',
    email: '',
    password: '',
    passwordConfirmation: '',
};

const initialTouched: RegisterFormTouched = {
    login: false,
    email: false,
    password: false,
    passwordConfirmation: false,
};

const initialErrors: RegisterFormErrors = {
    login: '',
    email: '',
    password: '',
    passwordConfirmation: '',
};

export function Register() {
    const navigate = useNavigate();
    const [values, setValues] = useState<RegisterFormValues>(initialValues);
    const [touched, setTouched] = useState<RegisterFormTouched>(initialTouched);
    const [errors, setErrors] = useState<RegisterFormErrors>(initialErrors);
    const [formError, setFormError] = useState('');

    const validateField = (field: keyof RegisterFormValues, value: string, password = values.password): string => {
        if (field === 'login') {
            return validateLogin(value);
        }

        if (field === 'email') {
            return validateEmail(value);
        }

        if (field === 'password') {
            return validatePassword(value);
        }

        return validatePasswordConfirmation(value, password);
    };

    const handleFieldChange = (field: keyof RegisterFormValues, value: string) => {
        setFormError('');
        const nextValues = {
            ...values,
            [field]: value,
        };

        setValues(nextValues);

        setErrors((currentErrors) => {
            const nextErrors = {
                ...currentErrors,
            };

            if (touched[field]) {
                nextErrors[field] = validateField(field, value, nextValues.password);
            }

            if (field === 'password' && touched.passwordConfirmation) {
                nextErrors.passwordConfirmation = validatePasswordConfirmation(
                    nextValues.passwordConfirmation,
                    nextValues.password,
                );
            }

            return nextErrors;
        });
    };

    const handleFieldBlur = (field: keyof RegisterFormValues) => {
        setTouched((currentTouched) => ({
            ...currentTouched,
            [field]: true,
        }));

        setErrors((currentErrors) => ({
            ...currentErrors,
            [field]: validateField(field, values[field]),
        }));
    };

    const getErrorText = (error: unknown): string => {
        if (!axios.isAxiosError(error)) {
            return '';
        }

        const responseData = error.response?.data;

        if (!responseData || typeof responseData !== 'object') {
            return '';
        }

        const { error: errorText } = responseData as ApiErrorResponse;

        return typeof errorText === 'string' ? errorText : '';
    };

    const handleSubmit = async () => {
        const nextErrors: RegisterFormErrors = {
            login: validateLogin(values.login),
            email: validateEmail(values.email),
            password: validatePassword(values.password),
            passwordConfirmation: validatePasswordConfirmation(values.passwordConfirmation, values.password),
        };

        setTouched({
            login: true,
            email: true,
            password: true,
            passwordConfirmation: true,
        });
        setErrors(nextErrors);

        if (nextErrors.login || nextErrors.email || nextErrors.password || nextErrors.passwordConfirmation) {
            return;
        }

        try {
            setFormError('');
            await register(values);
            navigate(routes.login());
        } catch (error) {
            const status = axios.isAxiosError(error) ? error.response?.status : undefined;
            const errorText = getErrorText(error);
            const lowerCaseErrorText = errorText.toLowerCase();

            if (status === 409 && lowerCaseErrorText.includes('логин')) {
                setTouched((currentTouched) => ({
                    ...currentTouched,
                    login: true,
                }));
                setErrors((currentErrors) => ({
                    ...currentErrors,
                    login: errorText,
                }));
                return;
            }

            if (status === 409 && lowerCaseErrorText.includes('email')) {
                setTouched((currentTouched) => ({
                    ...currentTouched,
                    email: true,
                }));
                setErrors((currentErrors) => ({
                    ...currentErrors,
                    email: errorText,
                }));
                return;
            }

            if (status === 400 && errorText.includes('Пароли не совпадают')) {
                setTouched((currentTouched) => ({
                    ...currentTouched,
                    passwordConfirmation: true,
                }));
                setErrors((currentErrors) => ({
                    ...currentErrors,
                    passwordConfirmation: errorText,
                }));
                return;
            }

            setFormError('Не удалось зарегистрироваться. Попробуйте позже');
        }
    };

    const handleLoginClick = () => {
        navigate(routes.login());
    };

    return (
        <AuthPageLayout>
            <AuthCard title="Регистрация">
                <RegisterForm
                    values={values}
                    errors={errors}
                    formError={formError}
                    onSubmit={handleSubmit}
                    onLoginClick={handleLoginClick}
                    onLoginChange={(value) => handleFieldChange('login', value)}
                    onEmailChange={(value) => handleFieldChange('email', value)}
                    onPasswordChange={(value) => handleFieldChange('password', value)}
                    onPasswordConfirmationChange={(value) => handleFieldChange('passwordConfirmation', value)}
                    onLoginBlur={() => handleFieldBlur('login')}
                    onEmailBlur={() => handleFieldBlur('email')}
                    onPasswordBlur={() => handleFieldBlur('password')}
                    onPasswordConfirmationBlur={() => handleFieldBlur('passwordConfirmation')}
                />
            </AuthCard>
        </AuthPageLayout>
    );
}
