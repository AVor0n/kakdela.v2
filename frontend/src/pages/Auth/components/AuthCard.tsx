import { Box, Title } from '@hh.ru/magritte-ui';
import type { ReactNode } from 'react';

type AuthCardProps = {
    title: string;
    children: ReactNode;
};

export function AuthCard({ title, children }: AuthCardProps) {
    return (
        <Box
            width={500}
            p={24}
            style={{
                backgroundColor: 'white',
                borderRadius: '24px',
                border: '1px solid #eee',
            }}
        >
            <Title Element="h4" alignment="center">
                {title}
            </Title>

            {children}
        </Box>
    );
}
