import { Button, Flex, Input, PasswordInput } from '@hh.ru/magritte-ui';
import type { ComponentProps } from 'react';

type RegisterFormValues = {
    login: string;
    email: string;
    password: string;
    passwordConfirmation: string;
};

type RegisterFormErrors = Record<keyof RegisterFormValues, string>;
type RegisterInputChangeHandler = NonNullable<ComponentProps<typeof Input>['onChange']>;
type RegisterPasswordChangeHandler = NonNullable<ComponentProps<typeof PasswordInput>['onChange']>;

type RegisterFormProps = {
    values: RegisterFormValues;
    errors: RegisterFormErrors;
    formError: string;
    onSubmit: () => void;
    onLoginClick: () => void;
    onLoginChange: RegisterInputChangeHandler;
    onEmailChange: RegisterInputChangeHandler;
    onPasswordChange: RegisterPasswordChangeHandler;
    onPasswordConfirmationChange: RegisterPasswordChangeHandler;
    onLoginBlur: () => void;
    onEmailBlur: () => void;
    onPasswordBlur: () => void;
    onPasswordConfirmationBlur: () => void;
};

export function RegisterForm({
    values,
    errors,
    formError,
    onSubmit,
    onLoginClick,
    onLoginChange,
    onEmailChange,
    onPasswordChange,
    onPasswordConfirmationChange,
    onLoginBlur,
    onEmailBlur,
    onPasswordBlur,
    onPasswordConfirmationBlur,
}: RegisterFormProps) {
    return (
        <form
            onSubmit={(event) => {
                event.preventDefault();
                onSubmit();
            }}
        >
            <div style={{ marginTop: 16 }}>
                <Input
                    size="large"
                    placeholder="Логин"
                    value={values.login}
                    invalid={Boolean(errors.login)}
                    errorMessage={errors.login}
                    onChange={onLoginChange}
                    onBlur={onLoginBlur}
                />
            </div>

            <div style={{ marginTop: 16 }}>
                <Input
                    size="large"
                    placeholder="Почта"
                    value={values.email}
                    invalid={Boolean(errors.email)}
                    errorMessage={errors.email}
                    onChange={onEmailChange}
                    onBlur={onEmailBlur}
                />
            </div>

            <div style={{ marginTop: 12 }}>
                <PasswordInput
                    size="large"
                    placeholder="Пароль"
                    value={values.password}
                    invalid={Boolean(errors.password)}
                    errorMessage={errors.password}
                    onChange={onPasswordChange}
                    onBlur={onPasswordBlur}
                />
            </div>

            <div style={{ marginTop: 12 }}>
                <PasswordInput
                    size="large"
                    placeholder="Повторите пароль"
                    value={values.passwordConfirmation}
                    invalid={Boolean(errors.passwordConfirmation)}
                    errorMessage={errors.passwordConfirmation}
                    onChange={onPasswordConfirmationChange}
                    onBlur={onPasswordConfirmationBlur}
                />
            </div>

            {formError && (
                <div
                    role="alert"
                    style={{ marginTop: 12, color: '#d6001c' }}
                >
                    {formError}
                </div>
            )}

            <Flex
                direction="column"
                gap={12}
                style={{ marginTop: 20 }}
            >
                <Button
                    style="accent"
                    mode="primary"
                    type="submit"
                >
                    Зарегистрироваться
                </Button>

                <Button
                    style="accent"
                    mode="tertiary"
                    type="button"
                    onClick={onLoginClick}
                >
                    К входу
                </Button>
            </Flex>
        </form>
    );
}
