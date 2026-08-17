import { deleteSurveyPage } from '@/api/surveyPages';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { deletePage } from '@/entities/Pages/Pages.slice';
import type { Page } from '@/shared/types/Survey.type';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import type { DraggableAttributes, DraggableSyntheticListeners } from '@dnd-kit/core';
import type { MouseEventHandler, Ref } from 'react';
import style from './PageSeparator.module.css';
import { PageDetail } from './components/PageDetail/PageDetail';
import { useAppSelector } from '@/hooks/useAppSelector';
import { findPageConditionReferences } from '@/shared/utils/conditions';
import { PageSeparatorFrame } from './PageSeparatorFrame';

interface Props {
    page: Page;
    conditionsEditorId: string;
    isConditionsEditorOpen: boolean;
    onToggleConditions: () => void;
    dragHandleAttributes?: DraggableAttributes;
    dragHandleListeners?: DraggableSyntheticListeners;
    dragHandleRef?: Ref<HTMLDivElement>;
}

const LogicIcon = () => (
    <svg
        xmlns='http://www.w3.org/2000/svg'
        width='24'
        height='24'
        viewBox='0 0 16 15'
        fill='currentColor'
        aria-hidden='true'
    >
        <path d='M12.36 10.826H12c-1.58 0-2.36-.747-3.34-1.692-.44-.413-.9-.865-1.44-1.24.6-.392 1.08-.865 1.52-1.297.96-.925 1.72-1.673 3.26-1.673h.36v2.083L16 3.94 12.36.875v2.082H12c-2.36 0-3.58 1.18-4.66 2.242-.98.944-1.76 1.692-3.34 1.692h-.26A2 2 0 0 0 2 5.908c-1.1 0-2 .885-2 1.967s.9 1.967 2 1.967c.74 0 1.38-.394 1.74-.983H4c1.54 0 2.3.747 3.26 1.671C8.3 11.535 9.6 12.793 12 12.793h.36v2.082L16 11.809l-3.64-3.066z' />
    </svg>
);

export function PageSeparator({
    page,
    conditionsEditorId,
    isConditionsEditorOpen,
    onToggleConditions,
    dragHandleAttributes,
    dragHandleListeners,
    dragHandleRef,
}: Props) {
    const dispatch = useAppDispatch();
    const surveyPages = useAppSelector((state) => state.pages.pages);
    const stopClickPropagation: MouseEventHandler<HTMLElement> = (event) => {
        event.stopPropagation();
    };

    const deletePageHandler: MouseEventHandler<HTMLButtonElement> = (event) => {
        event.stopPropagation();
        const conditionReferences = findPageConditionReferences(surveyPages, page.id);
        if (conditionReferences.length > 0) {
            const sourcePageNumbers = [
                ...new Set(conditionReferences.map(({ pageSerialNumber }) => pageSerialNumber)),
            ].sort((firstPageNumber, secondPageNumber) => firstPageNumber - secondPageNumber);
            const sourcePageLabel =
                sourcePageNumbers.length === 1
                    ? `странице ${sourcePageNumbers[0]}`
                    : `страницах ${sourcePageNumbers.join(', ')}`;
            dispatch(
                setErrorMessage({
                    message: `Нельзя удалить страницу: сначала удалите или измените переходы на ${sourcePageLabel}`,
                }),
            );
            return;
        }

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
            <PageSeparatorFrame
                overlay={
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
                }
                leadingAction={
                    <button
                        className={`${style.action} ${style.deleteAction}`}
                        type='button'
                        aria-label={`Удалить страницу ${page.serialNumber}`}
                        onClick={deletePageHandler}
                    >
                        <img src='/trash.svg' alt='' />
                    </button>
                }
                trailingAction={
                    <button
                        className={`${style.action} ${style.logicAction}`}
                        type='button'
                        aria-label={`${isConditionsEditorOpen ? 'Закрыть' : 'Открыть'} логику перехода`}
                        aria-expanded={isConditionsEditorOpen}
                        aria-controls={conditionsEditorId}
                        onClick={(event) => {
                            event.stopPropagation();
                            onToggleConditions();
                        }}
                    >
                        <LogicIcon />
                    </button>
                }
            >
                Страница {page.serialNumber}
            </PageSeparatorFrame>
            <PageDetail page={page} />
        </>
    );
}
