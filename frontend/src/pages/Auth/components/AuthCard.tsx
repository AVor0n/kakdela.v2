import { Title } from '@hh.ru/magritte-ui';
import type { ReactNode } from 'react';
import style from './AuthCard.module.css';

type AuthCardProps = {
    title: string;
    children: ReactNode;
};

export function AuthCard({ title, children }: AuthCardProps) {
    return (
        <div className={style.card}>
            <Title Element='h4' alignment='center'>
                {title}
            </Title>

            {children}
        </div>
    );
}
