import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { forgotPassword, resetPassword } from '@/api/auth';
import { routes } from '@/app/routes';
import { AuthCard } from '@/pages/Auth/components/AuthCard';
import { AuthPageLayout } from '@/pages/Auth/components/AuthPageLayout';
import { ForgotPasswordForm } from '@/pages/Auth/ForgotPassword/ForgotPasswordForm';
import {
    validateEmail,
    validatePassword,
    validatePasswordConfirmation,
    validateResetCode,
} from '@/pages/Auth/validation';

type ForgotPasswordFormValues = {
    email: string;
    code: string;
    newPassword: string;
    passwordConfirmation: string;
};

type ForgotPasswordFormTouched = Record<keyof ForgotPasswordFormValues, boolean>;
type ForgotPasswordFormErrors = Record<keyof ForgotPasswordFormValues, string>;

const initialValues: ForgotPasswordFormValues = {
    email: '',
    code: '',
    newPassword: '',
    passwordConfirmation: '',
};

const initialTouched: ForgotPasswordFormTouched = {
    email: false,
    code: false,
    newPassword: false,
    passwordConfirmation: false,
};

const initialErrors: ForgotPasswordFormErrors = {
    email: '',
    code: '',
    newPassword: '',
    passwordConfirmation: '',
};

export function ForgotPassword() {
    const navigate = useNavigate();
    const [values, setValues] = useState<ForgotPasswordFormValues>(initialValues);
    const [touched, setTouched] = useState<ForgotPasswordFormTouched>(initialTouched);
    const [errors, setErrors] = useState<ForgotPasswordFormErrors>(initialErrors);
    const [formError, setFormError] = useState('');
    const [isCodeSent, setIsCodeSent] = useState(false);

    const validateField = (
        field: keyof ForgotPasswordFormValues,
        value: string,
        newPassword = values.newPassword,
    ): string => {
        if (field === 'email') {
            return validateEmail(value);
        }

        if (field === 'code') {
            return validateResetCode(value);
        }

        if (field === 'newPassword') {
            return validatePassword(value);
        }

        return validatePasswordConfirmation(value, newPassword);
    };

    const handleFieldChange = (field: keyof ForgotPasswordFormValues, value: string) => {
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
                nextErrors[field] = validateField(field, value, nextValues.newPassword);
            }

            if (field === 'newPassword' && touched.passwordConfirmation) {
                nextErrors.passwordConfirmation = validatePasswordConfirmation(
                    nextValues.passwordConfirmation,
                    nextValues.newPassword,
                );
            }

            return nextErrors;
        });
    };

    const handleFieldBlur = (field: keyof ForgotPasswordFormValues) => {
        setTouched((currentTouched) => ({
            ...currentTouched,
            [field]: true,
        }));

        setErrors((currentErrors) => ({
            ...currentErrors,
            [field]: validateField(field, values[field]),
        }));
    };

    const handleRequestCode = async () => {
        const emailError = validateEmail(values.email);

        setTouched((currentTouched) => ({ ...currentTouched, email: true }));
        setErrors((currentErrors) => ({ ...currentErrors, email: emailError }));

        if (emailError) {
            return;
        }

        try {
            setFormError('');
            await forgotPassword(values.email);
            setIsCodeSent(true);
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 404) {
                setErrors((currentErrors) => ({ ...currentErrors, email: 'Аккаунт с такой почтой не найден' }));
                return;
            } else if (axios.isAxiosError(error) && error.response && error.response?.status === 429) {
                setErrors((currentErrors) => ({
                    ...currentErrors,
                    email: `Слишком много попыток. ${error.response?.data.message}`,
                }));
                return;
            }

            setFormError('Не удалось отправить письмо. Попробуйте позже');
        }
    };

    const handleResetPassword = async () => {
        const nextErrors: ForgotPasswordFormErrors = {
            ...errors,
            code: validateResetCode(values.code),
            newPassword: validatePassword(values.newPassword),
            passwordConfirmation: validatePasswordConfirmation(values.passwordConfirmation, values.newPassword),
        };

        setTouched((currentTouched) => ({
            ...currentTouched,
            code: true,
            newPassword: true,
            passwordConfirmation: true,
        }));
        setErrors(nextErrors);

        if (nextErrors.code || nextErrors.newPassword || nextErrors.passwordConfirmation) {
            return;
        }

        try {
            setFormError('');
            await resetPassword({
                email: values.email,
                code: values.code,
                newPassword: values.newPassword,
                passwordConfirmation: values.passwordConfirmation,
            });
            navigate(routes.login());
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 400) {
                setErrors((currentErrors) => ({ ...currentErrors, code: 'Код подтверждения неверный или истёк' }));
                return;
            }

            setFormError('Не удалось сохранить новый пароль. Попробуйте позже');
        }
    };

    const handleSubmit = () => {
        if (isCodeSent) {
            void handleResetPassword();
            return;
        }

        void handleRequestCode();
    };

    return (
        <AuthPageLayout>
            <AuthCard title='Сброс пароля'>
                <ForgotPasswordForm
                    values={values}
                    errors={errors}
                    formError={formError}
                    isCodeSent={isCodeSent}
                    onSubmit={handleSubmit}
                    onEmailChange={(value) => handleFieldChange('email', value)}
                    onCodeChange={(value) => handleFieldChange('code', value)}
                    onNewPasswordChange={(value) => handleFieldChange('newPassword', value)}
                    onPasswordConfirmationChange={(value) => handleFieldChange('passwordConfirmation', value)}
                    onEmailBlur={() => handleFieldBlur('email')}
                    onCodeBlur={() => handleFieldBlur('code')}
                    onNewPasswordBlur={() => handleFieldBlur('newPassword')}
                    onPasswordConfirmationBlur={() => handleFieldBlur('passwordConfirmation')}
                />
            </AuthCard>
        </AuthPageLayout>
    );
}
