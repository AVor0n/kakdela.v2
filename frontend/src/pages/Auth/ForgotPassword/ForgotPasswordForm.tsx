import { Button, Flex, Input } from '@hh.ru/magritte-ui';
import type { ComponentProps } from 'react';

type ForgotPasswordInputChangeHandler = NonNullable<ComponentProps<typeof Input>['onChange']>;

type ForgotPasswordFormProps = {
    email: string;
    emailError: string;
    formError: string;
    onSubmit: () => void;
    onEmailChange: ForgotPasswordInputChangeHandler;
    onEmailBlur: () => void;
};

export function ForgotPasswordForm({
    email,
    emailError,
    formError,
    onSubmit,
    onEmailChange,
    onEmailBlur,
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
                    value={email}
                    invalid={Boolean(emailError)}
                    errorMessage={emailError}
                    onChange={onEmailChange}
                    onBlur={onEmailBlur}
                />
            </div>

            {formError && (
                <div role='alert' style={{ marginTop: 12, color: '#d6001c' }}>
                    {formError}
                </div>
            )}

            <Flex direction='column' gap={12} style={{ marginTop: 20 }}>
                <Button style='accent' mode='primary' type='submit'>
                    Отправить код
                </Button>
            </Flex>
        </form>
    );
}
