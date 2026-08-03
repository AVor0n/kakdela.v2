import { Flex } from '@hh.ru/magritte-ui';
import type { ReactNode } from 'react';
import style from './AuthPageLayout.module.css';

type AuthPageLayoutProps = {
    children: ReactNode;
};

export function AuthPageLayout({ children }: AuthPageLayoutProps) {
    return (
        <Flex
            direction='column'
            align='center'
            justify='center'
            width='100%'
            minHeight='100dvh'
            className={style.layout}
        >
            {children}
        </Flex>
    );
}
