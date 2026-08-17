import { Button, Flex, Input, Text } from '@hh.ru/magritte-ui';
import type { ComponentProps } from 'react';

type ForgotPasswordInputChangeHandler = NonNullable<ComponentProps<typeof Input>['onChange']>;
type ForgotPasswordInputBlurHandler = NonNullable<ComponentProps<typeof Input>['onBlur']>;

interface Props {
    email: string;
    code: string;
    onSubmit: () => void;
    codeError: string;
    formError: string;
    onCodeChange: ForgotPasswordInputChangeHandler;
    onCodeBlur: ForgotPasswordInputBlurHandler;
}

export function VerifyCodeForm({ email, code, onSubmit, onCodeChange, onCodeBlur, codeError, formError }: Props) {
    return (
        <form
            onSubmit={(e) => {
                e.preventDefault();
                onSubmit();
            }}
            style={{ textAlign: 'center' }}
        >
            <Text typography='subtitle-3-semibold' style='secondary'>
                Введите код подтверждения отправленный на почту {email}
            </Text>
            <div style={{ marginTop: 16 }}>
                <Input
                    size='large'
                    value={code}
                    onChange={onCodeChange}
                    onBlur={onCodeBlur}
                    invalid={Boolean(codeError)}
                    errorMessage={codeError}
                    placeholder='Код подтверждения'
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
