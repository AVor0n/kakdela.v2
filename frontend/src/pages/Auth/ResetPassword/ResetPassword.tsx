import axios from 'axios';
import { useState } from 'react';
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { AuthPageLayout } from '../components/AuthPageLayout';
import { AuthCard } from '../components/AuthCard';
import { ResetPasswordForm } from './ResetPasswordForm';
import { routes } from '@/app/routes';
import { resetPassword } from '@/api/auth';
import { validatePassword, validatePasswordConfirmation } from '@/pages/Auth/validation';

type ResetPasswordFormValues = {
    newPassword: string;
    confirmPassword: string;
};

type ResetPasswordFormTouched = Record<keyof ResetPasswordFormValues, boolean>;
type ResetPasswordFormErrors = Record<keyof ResetPasswordFormValues, string>;

const initialValues: ResetPasswordFormValues = {
    newPassword: '',
    confirmPassword: '',
};

const initialTouched: ResetPasswordFormTouched = {
    newPassword: false,
    confirmPassword: false,
};

const initialErrors: ResetPasswordFormErrors = {
    newPassword: '',
    confirmPassword: '',
};

export function ResetPassword() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const email = searchParams.get('email');
    const code = searchParams.get('code');

    const [values, setValues] = useState<ResetPasswordFormValues>(initialValues);
    const [touched, setTouched] = useState<ResetPasswordFormTouched>(initialTouched);
    const [errors, setErrors] = useState<ResetPasswordFormErrors>(initialErrors);
    const [formError, setFormError] = useState('');

    if (!email || !code) {
        return <Navigate to={routes.root()} />;
    }

    const validateField = (field: keyof ResetPasswordFormValues, value: string): string => {
        if (field === 'newPassword') {
            return validatePassword(value);
        }

        return validatePasswordConfirmation(value, values.newPassword);
    };

    const handleFieldChange = (field: keyof ResetPasswordFormValues, value: string) => {
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

    const handleFieldBlur = (field: keyof ResetPasswordFormValues) => {
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
        const nextErrors: ResetPasswordFormErrors = {
            newPassword: validatePassword(values.newPassword),
            confirmPassword: validatePasswordConfirmation(values.confirmPassword, values.newPassword),
        };

        setTouched({
            newPassword: true,
            confirmPassword: true,
        });
        setErrors(nextErrors);

        if (nextErrors.newPassword || nextErrors.confirmPassword) {
            return;
        }

        try {
            setFormError('');
            await resetPassword({
                email,
                code,
                newPassword: values.newPassword,
                passwordConfirmation: values.confirmPassword,
            });

            navigate(routes.login(), { replace: true });
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 429) {
                setFormError(`Слишком много попыток. ${error.response?.data.message}`);
                return;
            }

            setFormError('Не удалось изменить пароль. Попробуйте позже');
        }
    };

    return (
        <AuthPageLayout>
            <AuthCard title='Новый пароль'>
                <ResetPasswordForm
                    values={values}
                    errors={errors}
                    formError={formError}
                    onSubmit={handleSubmit}
                    onNewPasswordChange={(value) => handleFieldChange('newPassword', value)}
                    onConfirmPasswordChange={(value) => handleFieldChange('confirmPassword', value)}
                    onNewPasswordBlur={() => handleFieldBlur('newPassword')}
                    onConfirmPasswordBlur={() => handleFieldBlur('confirmPassword')}
                />
            </AuthCard>
        </AuthPageLayout>
    );
}
