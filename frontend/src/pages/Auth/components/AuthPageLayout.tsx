import { Flex } from '@hh.ru/magritte-ui';
import type { ReactNode } from 'react';

type AuthPageLayoutProps = {
    children: ReactNode;
};

export function AuthPageLayout({ children }: AuthPageLayoutProps) {
    return (
        <Flex align='center' justify='center' style={{ minHeight: '100vh' }}>
            {children}
        </Flex>
    );
}
