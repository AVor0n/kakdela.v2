import type { AnswerOption } from '@/shared/types/Question.type';
import type { DraggableAttributes, DraggableSyntheticListeners } from '@dnd-kit/core';
import { useState, type MouseEventHandler, type ReactNode, type Ref } from 'react';
import { deleteAnswerOption, updateAnswerOption } from '@/api/answer-option';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { deleteOption, setOptionValue } from '@/entities/Survey/Survey.slice';

import { Button } from '@hh.ru/magritte-ui';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './Option.module.css';
interface Props {
    option: AnswerOption;
    children: ReactNode;
    isEdit: boolean;
    dragHandleAttributes?: DraggableAttributes;
    dragHandleListeners?: DraggableSyntheticListeners;
    dragHandleRef?: Ref<HTMLDivElement>;
}

export function Option({
    option,
    children,
    isEdit,
    dragHandleAttributes,
    dragHandleListeners,
    dragHandleRef,
}: Props) {
    const [optionAnswer, setOptionAnswer] = useState<string>(option.answerOptionText);

    const dispatch = useAppDispatch();
    const stopClickPropagation: MouseEventHandler<HTMLDivElement> = (event) => {
        event.stopPropagation();
    };

    const deleteAnswerOptionHandler = () => {
        deleteAnswerOption(option.id)
            .then(() => {
                dispatch(deleteOption({ id: option.id }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: `Не удалось удалить варинат ответа` }));
                }
            });
    };

    const updateQuestionOptionHandler = () => {
        if (optionAnswer !== option.answerOptionText) {
            updateAnswerOption(option.id, { serialNumber: option.serialNumber, answerOptionText: optionAnswer })
                .then((data) => dispatch(setOptionValue({ answerOption: data })))
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: `Не удалось изменить варинат ответа` }));
                    }

                    dispatch(
                        setOptionValue({
                            answerOption: { ...option, answerOptionText: option.answerOptionText },
                        }),
                    );
                });
        }
    };
    return (
        <>
            <div className={style.optionContent}>
                <label className={style.option}>
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
                    {option.answerOptionText !== 'Другое' ? (
                        <input
                            className={style.input}
                            value={optionAnswer}
                            onChange={(e) => {
                                setOptionAnswer(e.target.value);
                            }}
                            onBlur={updateQuestionOptionHandler}
                        />
                    ) : (
                        <div>
                            <span>Другое: </span>
                            <input className={style.another} disabled />
                        </div>
                    )}
                </label>

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
