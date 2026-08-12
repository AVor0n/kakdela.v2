import { Button, Flex, Input, PasswordInput } from '@hh.ru/magritte-ui';
import type { ComponentProps } from 'react';

type ForgotPasswordFormValues = {
    email: string;
    code: string;
    newPassword: string;
    passwordConfirmation: string;
};

type ForgotPasswordFormErrors = Record<keyof ForgotPasswordFormValues, string>;
type ForgotPasswordInputChangeHandler = NonNullable<ComponentProps<typeof Input>['onChange']>;
type ForgotPasswordPasswordChangeHandler = NonNullable<ComponentProps<typeof PasswordInput>['onChange']>;

type ForgotPasswordFormProps = {
    values: ForgotPasswordFormValues;
    errors: ForgotPasswordFormErrors;
    formError: string;
    isCodeSent: boolean;
    onSubmit: () => void;
    onEmailChange: ForgotPasswordInputChangeHandler;
    onCodeChange: ForgotPasswordInputChangeHandler;
    onNewPasswordChange: ForgotPasswordPasswordChangeHandler;
    onPasswordConfirmationChange: ForgotPasswordPasswordChangeHandler;
    onEmailBlur: () => void;
    onCodeBlur: () => void;
    onNewPasswordBlur: () => void;
    onPasswordConfirmationBlur: () => void;
};

export function ForgotPasswordForm({
    values,
    errors,
    formError,
    isCodeSent,
    onSubmit,
    onEmailChange,
    onCodeChange,
    onNewPasswordChange,
    onPasswordConfirmationChange,
    onEmailBlur,
    onCodeBlur,
    onNewPasswordBlur,
    onPasswordConfirmationBlur,
}: ForgotPasswordFormProps) {
    return (
        <form
            onSubmit={(event) => {
                event.preventDefault();
                onSubmit();
            }}
        >
            <div style={{ marginTop: 16 }}>
                <Input
                    size='large'
                    placeholder='Почта'
                    value={values.email}
                    disabled={isCodeSent}
                    invalid={Boolean(errors.email)}
                    errorMessage={errors.email}
                    onChange={onEmailChange}
                    onBlur={onEmailBlur}
                />
            </div>

            {isCodeSent && (
                <>
                    <div style={{ marginTop: 16 }}>
                        <Input
                            size='large'
                            placeholder='Код из письма'
                            value={values.code}
                            invalid={Boolean(errors.code)}
                            errorMessage={errors.code}
                            onChange={onCodeChange}
                            onBlur={onCodeBlur}
                        />
                    </div>

                    <div style={{ marginTop: 12 }}>
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
                            placeholder='Повторите новый пароль'
                            value={values.passwordConfirmation}
                            invalid={Boolean(errors.passwordConfirmation)}
                            errorMessage={errors.passwordConfirmation}
                            onChange={onPasswordConfirmationChange}
                            onBlur={onPasswordConfirmationBlur}
                        />
                    </div>
                </>
            )}

            {formError && (
                <div role='alert' style={{ marginTop: 12, color: '#d6001c' }}>
                    {formError}
                </div>
            )}

            <Flex direction='column' gap={12} style={{ marginTop: 20 }}>
                <Button style='accent' mode='primary' type='submit'>
                    {isCodeSent ? 'Сохранить новый пароль' : 'Сбросить'}
                </Button>
            </Flex>
        </form>
    );
}
