import type { AnswerOption } from '@/shared/types/Question.type';
import type { DraggableAttributes, DraggableSyntheticListeners } from '@dnd-kit/core';
import { useState, type MouseEventHandler, type ReactNode, type Ref } from 'react';
import { deleteAnswerOption, updateAnswerOption } from '@/api/answer-option';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { deleteOption, setOptionValue, setPage } from '@/entities/Pages/Pages.slice';

import { Button } from '@hh.ru/magritte-ui';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './Option.module.css';
import { EditorInput } from '@/shared/ui/EditorInput/EditorInput';
import { useAppSelector } from '@/hooks/useAppSelector';
import { isAnswerOptionUsedInConditions } from '@/shared/utils/conditions';
import { getSurveyPageForEdit } from '@/api/surveyPages';

interface Props {
    option: AnswerOption;
    children: ReactNode;
    isEdit: boolean;
    dragHandleAttributes?: DraggableAttributes;
    dragHandleListeners?: DraggableSyntheticListeners;
    dragHandleRef?: Ref<HTMLDivElement>;
}

export function Option({ option, children, isEdit, dragHandleAttributes, dragHandleListeners, dragHandleRef }: Props) {
    const [optionAnswer, setOptionAnswer] = useState<string>(option.text);
    const pages = useAppSelector((state) => state.pages.pages);

    const dispatch = useAppDispatch();
    const stopClickPropagation: MouseEventHandler<HTMLDivElement> = (event) => {
        event.stopPropagation();
    };

    const deleteAnswerOptionHandler = () => {
        const pageId = pages.find((page) =>
            page.questions.some(
                (question) =>
                    (question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') &&
                    question.answerOptions.some(({ id }) => id === option.id),
            ),
        )?.id;
        if (
            isAnswerOptionUsedInConditions(pages, option.id) &&
            !window.confirm('Этот вариант ответа используется в логике перехода. Всё равно удалить его?')
        ) {
            return;
        }
        deleteAnswerOption(option.id)
            .then(() => {
                dispatch(deleteOption({ id: option.id }));
                if (pageId) {
                    void getSurveyPageForEdit(pageId)
                        .then((page) => dispatch(setPage({ page })))
                        .catch(() =>
                            dispatch(setErrorMessage({ message: 'Вариант удалён, но не удалось обновить условия' })),
                        );
                }
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: `Не удалось удалить варинат ответа` }));
                }
            });
    };

    const updateQuestionOptionHandler = () => {
        if (optionAnswer !== option.text) {
            updateAnswerOption(option.id, { serialNumber: option.serialNumber, text: optionAnswer })
                .then((data) => dispatch(setOptionValue({ answerOption: data })))
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: `Не удалось изменить варинат ответа` }));
                    }

                    dispatch(
                        setOptionValue({
                            answerOption: { ...option, text: option.text },
                        }),
                    );
                });
        }
    };
    return (
        <>
            <div className={style.optionContent}>
                <div className={style.option}>
                    {isEdit && (
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
                    )}
                    {children}
                    <EditorInput
                        className={style.input}
                        value={optionAnswer}
                        onChange={setOptionAnswer}
                        onBlur={updateQuestionOptionHandler}
                        isTextColor
                    />
                </div>

                {isEdit && (
                    <Button
                        onClick={deleteAnswerOptionHandler}
                        mode='secondary'
                        style='negative'
                        icon={<img src='/X.svg' alt='X' />}
                        size='small'
                        className=''
                    />
                )}
            </div>
        </>
    );
}
