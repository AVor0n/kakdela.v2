import { deleteSurveyPage } from '@/api/surveyPages';
import style from './PageSeparator.module.css';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { deletePage } from '@/entities/Survey/Survey.slice';
import { useState } from 'react';
import { ErrorBlock } from '../ErrorBlock/ErrorBlock';
import type { Page } from '@/shared/types/Survey.type';
import type { Error } from '@/shared/types/Error.type';

interface Props {
    page: Page;
}

export function PageSeparator({ page }: Props) {
    const [error, setError] = useState<Error | null>(null);
    const dispatch = useAppDispatch();
    const deletePageHandler = () => {
        deleteSurveyPage(page.id)
            .then(() => {
                dispatch(deletePage({ pageId: page.id }));
            })
            .catch((err) => {
                if (err.response) {
                    setError(err.response.data);
                }
            });
    };

    return (
        <>
            {error && <ErrorBlock error={error} setError={setError} />}
            <div className={style.separator}>
                <span className={style.content}>Страница {page.serialNumber}</span>
                <img className={style.trash} src='/trash.svg' alt='X' onClick={deletePageHandler} />
            </div>
        </>
    );
}
