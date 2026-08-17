import { Button, Flex, PasswordInput } from '@hh.ru/magritte-ui';
import type { ComponentProps } from 'react';

type ResetPasswordFormValues = {
    newPassword: string;
    confirmPassword: string;
};

type ResetPasswordFormErrors = Record<keyof ResetPasswordFormValues, string>;
type ResetPasswordChangeHandler = NonNullable<ComponentProps<typeof PasswordInput>['onChange']>;

type Props = {
    values: ResetPasswordFormValues;
    errors: ResetPasswordFormErrors;
    formError: string;
    onSubmit: () => void;
    onNewPasswordChange: ResetPasswordChangeHandler;
    onConfirmPasswordChange: ResetPasswordChangeHandler;
    onNewPasswordBlur: () => void;
    onConfirmPasswordBlur: () => void;
};

export function ResetPasswordForm({
    values,
    errors,
    formError,
    onSubmit,
    onNewPasswordChange,
    onConfirmPasswordChange,
    onNewPasswordBlur,
    onConfirmPasswordBlur,
}: Props) {
    return (
        <form
            onSubmit={(event) => {
                event.preventDefault();
                onSubmit();
            }}
        >
            <div style={{ marginTop: 16 }}>
                <PasswordInput
                    size='large'
                    placeholder='Новый пароль'
                    value={values.newPassword}
                    invalid={Boolean(errors.newPassword)}
                    errorMessage={errors.newPassword}
                    onChange={onNewPasswordChange}
                    onBlur={onNewPasswordBlur}
                />
            </div>

            <div style={{ marginTop: 12 }}>
                <PasswordInput
                    size='large'
                    placeholder='Повторите пароль'
                    value={values.confirmPassword}
                    invalid={Boolean(errors.confirmPassword)}
                    errorMessage={errors.confirmPassword}
                    onChange={onConfirmPasswordChange}
                    onBlur={onConfirmPasswordBlur}
                />
            </div>

            {formError && (
                <div role='alert' style={{ marginTop: 12, color: '#d6001c' }}>
                    {formError}
                </div>
            )}

            <Flex direction='column' gap={12} style={{ marginTop: 20 }}>
                <Button style='accent' mode='primary' type='submit'>
                    Сохранить пароль
                </Button>
            </Flex>
        </form>
    );
}
