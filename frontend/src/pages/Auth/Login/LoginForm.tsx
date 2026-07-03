import { Button, Flex, Input, PasswordInput } from '@hh.ru/magritte-ui';
import type { ComponentProps } from 'react';

type LoginFormValues = {
    login: string;
    password: string;
};

type LoginFormErrors = Record<keyof LoginFormValues, string>;
type LoginInputChangeHandler = NonNullable<ComponentProps<typeof Input>['onChange']>;
type LoginPasswordChangeHandler = NonNullable<ComponentProps<typeof PasswordInput>['onChange']>;

type LoginFormProps = {
    values: LoginFormValues;
    errors: LoginFormErrors;
    formError: string;
    onSubmit: () => void;
    onRegisterClick: () => void;
    onLoginChange: LoginInputChangeHandler;
    onPasswordChange: LoginPasswordChangeHandler;
    onLoginBlur: () => void;
    onPasswordBlur: () => void;
};

export function LoginForm({
    values,
    errors,
    formError,
    onSubmit,
    onRegisterClick,
    onLoginChange,
    onPasswordChange,
    onLoginBlur,
    onPasswordBlur,
}: LoginFormProps) {
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
                    placeholder='Логин'
                    value={values.login}
                    invalid={Boolean(errors.login)}
                    errorMessage={errors.login}
                    onChange={onLoginChange}
                    onBlur={onLoginBlur}
                />
            </div>

            <div style={{ marginTop: 12 }}>
                <PasswordInput
                    size='large'
                    placeholder='Пароль'
                    value={values.password}
                    invalid={Boolean(errors.password)}
                    errorMessage={errors.password}
                    onChange={onPasswordChange}
                    onBlur={onPasswordBlur}
                />
            </div>

            {formError && (
                <div role='alert' style={{ marginTop: 12, color: '#d6001c' }}>
                    {formError}
                </div>
            )}

            <Flex direction='column' gap={12} style={{ marginTop: 20 }}>
                <Button style='accent' mode='primary' type='submit'>
                    Войти
                </Button>

                <Button style='accent' mode='tertiary' type='button' onClick={onRegisterClick}>
                    Зарегистрироваться
                </Button>
            </Flex>
        </form>
    );
}
