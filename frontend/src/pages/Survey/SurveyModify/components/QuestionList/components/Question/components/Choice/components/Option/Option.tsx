import type { AnswerOption } from '@/shared/types/Question.type';
import { useEffect, useState, type ReactNode } from 'react';
import { deleteAnswerOption, updateAnswerOption } from '@/api/answer-option';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { deleteOption, setOptionValue } from '@/entities/Survey/Survey.slice';
import { useDebounce } from '@/hooks/useDebounce';
import { Button } from '@hh.ru/magritte-ui';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './Option.module.css';
interface Props {
    option: AnswerOption;
    children: ReactNode;
    isEdit: boolean;
}

export function Option({ option, children, isEdit }: Props) {
    const [optionAnswer, setOptionAnswer] = useState<string>(option.answerOptionText);
    const debouncedOptionAnswer = useDebounce(optionAnswer, 2000);
    const dispatch = useAppDispatch();
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

    useEffect(() => {
        if (debouncedOptionAnswer !== option.answerOptionText) {
            const handler = setTimeout(() => {
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
            }, 2000);
            return () => {
                clearTimeout(handler);
            };
        }
    }, [debouncedOptionAnswer]);
    return (
        <>
            <div className={style.optionContent}>
                <label className={style.option}>
                    {children}
                    {option.answerOptionText !== 'Другое' ? (
                        <input
                            className={style.input}
                            value={optionAnswer}
                            onChange={(e) => {
                                setOptionAnswer(e.target.value);
                            }}
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
