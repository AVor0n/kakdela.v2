import { Text, Title } from '@hh.ru/magritte-ui';
import type { ReactNode } from 'react';
import { HTMLRender } from '@/shared/ui/HTMLRender/HTMLRender';
import style from './SurveyFlowPage.module.css';

type Props = {
    title: string | null;
    description: string | null;
    fallbackTitle?: string;
    fallbackDescription?: string;
    media?: ReactNode;
    children?: ReactNode;
};

export function SurveyFlowPage({ title, description, fallbackTitle, fallbackDescription, media, children }: Props) {
    return (
        <section className={style.container}>
            {media}

            {title ? (
                <HTMLRender className={style.title} html={title} />
            ) : (
                fallbackTitle && (
                    <Title Element='h2' size='medium'>
                        {fallbackTitle}
                    </Title>
                )
            )}

            {description ? (
                <HTMLRender className={style.description} html={description} />
            ) : (
                fallbackDescription && (
                    <Text typography='paragraph-2-regular' style='primary'>
                        {fallbackDescription}
                    </Text>
                )
            )}

            {children}
        </section>
    );
}
