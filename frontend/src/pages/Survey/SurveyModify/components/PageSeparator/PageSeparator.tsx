import { deleteSurveyPage } from '@/api/surveyPages';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { deletePage } from '@/entities/Survey/Survey.slice';
import type { Page } from '@/shared/types/Survey.type';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import type { DraggableAttributes, DraggableSyntheticListeners } from '@dnd-kit/core';
import type { MouseEventHandler, Ref } from 'react';
import style from './PageSeparator.module.css';
import { PageDetail } from './components/PageDetail/PageDetail';
interface Props {
    page: Page;
    dragHandleAttributes?: DraggableAttributes;
    dragHandleListeners?: DraggableSyntheticListeners;
    dragHandleRef?: Ref<HTMLDivElement>;
}

export function PageSeparator({ page, dragHandleAttributes, dragHandleListeners, dragHandleRef }: Props) {
    const dispatch = useAppDispatch();
    const stopClickPropagation: MouseEventHandler<HTMLDivElement> = (event) => {
        event.stopPropagation();
    };

    const deletePageHandler = () => {
        deleteSurveyPage(page.id)
            .then(() => {
                dispatch(deletePage({ pageId: page.id }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: 'Не удалось удалить страницу' }));
                }
            });
    };

    return (
        <>
            <div className={style.separator}>
                <div
                    ref={dragHandleRef}
                    className={style.dragHandle}
                    onClick={stopClickPropagation}
                    {...dragHandleAttributes}
                    {...dragHandleListeners}
                >
                    <svg className={style.dragHandleIcon} viewBox='0 0 16 8' fill='none' aria-hidden='true'>
                        <circle cx='4' cy='2' r='1.5' fill='currentColor' />
                        <circle cx='8' cy='2' r='1.5' fill='currentColor' />
                        <circle cx='12' cy='2' r='1.5' fill='currentColor' />
                        <circle cx='4' cy='6' r='1.5' fill='currentColor' />
                        <circle cx='8' cy='6' r='1.5' fill='currentColor' />
                        <circle cx='12' cy='6' r='1.5' fill='currentColor' />
                    </svg>
                </div>
                <span className={style.content}>Страница {page.serialNumber}</span>
                <img className={style.trash} src='/trash.svg' alt='X' onClick={deletePageHandler} />
            </div>
            <PageDetail page={page} />
        </>
    );
}
