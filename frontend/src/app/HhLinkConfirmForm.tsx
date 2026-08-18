import { Button, Flex, PasswordInput, Text } from '@hh.ru/magritte-ui';
import type { ComponentProps } from 'react';

type HhLinkPasswordChangeHandler = NonNullable<ComponentProps<typeof PasswordInput>['onChange']>;

type HhLinkConfirmFormProps = {
    password: string;
    passwordError: string;
    formError: string;
    submitting: boolean;
    onSubmit: () => void;
    onPasswordChange: HhLinkPasswordChangeHandler;
    onPasswordBlur: () => void;
};

export function HhLinkConfirmForm({
    password,
    passwordError,
    formError,
    submitting,
    onSubmit,
    onPasswordChange,
    onPasswordBlur,
}: HhLinkConfirmFormProps) {
    return (
        <form
            onSubmit={(event) => {
                event.preventDefault();
                onSubmit();
            }}
            style={{ textAlign: 'center' }}
        >
            <Text typography='subtitle-3-semibold' style='secondary'>
                Аккаунт с таким email уже зарегистрирован. Введите пароль от него, чтобы привязать hh.ru
            </Text>

            <div style={{ marginTop: 16 }}>
                <PasswordInput
                    size='large'
                    placeholder='Пароль'
                    value={password}
                    invalid={Boolean(passwordError)}
                    errorMessage={passwordError}
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
                <Button style='accent' mode='primary' type='submit' loading={submitting} disabled={submitting}>
                    Подтвердить
                </Button>
            </Flex>
        </form>
    );
}
