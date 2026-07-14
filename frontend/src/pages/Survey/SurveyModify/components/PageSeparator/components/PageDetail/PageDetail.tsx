import { Input } from '@hh.ru/magritte-ui-input';
import { TextArea } from '@hh.ru/magritte-ui-textarea';
import type { Page } from '@/shared/types/Survey.type';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setPage } from '@/entities/Survey/Survey.slice';
import { useEffect, useState } from 'react';
import style from './PageDetail.module.css';
import { updateSurveyPage } from '@/api/surveyPages';
import { setErrorMessage } from '@/entities/Error/Error.slice';
interface Props {
    page: Page;
}

export function PageDetail({ page }: Props) {
    const [title, setTitle] = useState<string>(page.title ?? '');
    const [description, setDescription] = useState<string>(page.description ?? '');

    const dispatch = useAppDispatch();

    const updateTitleHandler = () => {
        if (title !== page.title) {
            updateSurveyPage(page.id, { title: title })
                .then((data) => {
                    dispatch(setPage({ page: data }));
                })
                .catch(() => {
                    setErrorMessage({ message: 'Не удалось поменять название страницы' });
                    dispatch(setPage({ page }));
                });
        }
    };

    const updateDescriptionHandler = () => {
        if (description !== page.description) {
            updateSurveyPage(page.id, { description: description })
                .then((data) => {
                    dispatch(setPage({ page: data }));
                })
                .catch(() => {
                    setErrorMessage({ message: 'Не удалось поменять описание страницы' });
                    dispatch(setPage({ page }));
                });
        }
    };

    useEffect(() => {
        return () => {
            updateTitleHandler();
            updateDescriptionHandler();
        };
    }, []);

    return (
        <div className={style.container}>
            <Input
                placeholder='Заголовок страницы'
                value={title}
                onChange={(value: string) => setTitle(value)}
                onBlur={updateTitleHandler}
            />

            <TextArea
                placeholder='Описание страницы'
                data-qa='textarea'
                layout='fixed'
                resize='none'
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                elevatePlaceholder={true}
                onBlur={updateDescriptionHandler}
            />
        </div>
    );
}
