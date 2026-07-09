import type { Question, QuestionType } from '@/shared/types/Question.type';
import {
    Button,
    Checkbox,
    createStaticDataProvider,
    Input,
    Select,
    type StaticDataFetcherItem,
} from '@hh.ru/magritte-ui';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { ShortText } from './components/ShortText/ShortText';
import { LongText } from './components/LongText/LongText';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import {
    deleteQuestion as deleteQuestionState,
    duplicateQuestion,
    setMandatory as setMandatoryState,
    updateQuestionTitle,
    updateQuestionType,
} from '@/entities/Survey/Survey.slice';
import { Choice } from './components/Choice/Choice';
import classNames from 'classnames';
import { useDebounce } from '@/hooks/useDebounce';
import { deleteQuestion, updateQuestion } from '@/api/question';
import { useAppSelector } from '@/hooks/useAppSelector';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './Question.module.css';

interface Props {
    question: Question;
    isEditMode: boolean;
    onClick?: () => void;
}

const OPTIONS: StaticDataFetcherItem[] = [
    { value: 'SHORT_TEXT', text: 'Короткий текст' },
    { value: 'LONG_TEXT', text: 'Длинный текст' },
    { value: 'SINGLE_CHOICE', text: 'Один из списка' },
    { value: 'MULTIPLE_CHOICE', text: 'Несколько из списка' },
];

export function Question({ question, onClick, isEditMode }: Props) {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const [title, setTitle] = useState<string>(question.title);
    const [typeQuestion, setTypeQuestion] = useState<QuestionType>(question.type);
    const [mandatory, setMandatory] = useState<boolean>(question.isMandatory);
    const debouncedMandatory = useDebounce(mandatory, 500);

    const dispatch = useAppDispatch();

    const updateQuestionTitleHandler = () => {
        if (title !== question.title) {
            updateQuestion(question.id, { title })
                .then((data) => {
                    dispatch(updateQuestionTitle({ id: question.id, title: data.title }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Не удалось изменить название вопроса' }));
                    }
                    setTitle(question.title);
                });
        }
    };

    const updateQuestionTypeHandler = () => {
        if (typeQuestion !== question.type) {
            updateQuestion(question.id, { type: typeQuestion })
                .then((data) => {
                    dispatch(
                        updateQuestionType({
                            id: question.id,
                            type: data.type,
                        }),
                    );
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Не удалось изменить тип вопроса' }));
                    }
                    setTypeQuestion(question.type);
                });
        }
    };

    useEffect(() => {
        if (debouncedMandatory !== question.isMandatory) {
            updateQuestion(question.id, { isMandatory: mandatory })
                .then((data) => {
                    dispatch(setMandatoryState({ value: data.isMandatory }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Не удалось изменить описание опроса' }));
                    }
                    dispatch(setMandatoryState({ value: question.isMandatory }));
                });
        }
    }, [debouncedMandatory]);

    const deleteQuestionHandler = () => {
        if (!selectedSurvey) return;
        deleteQuestion(question.id)
            .then(() => {
                dispatch(deleteQuestionState({ id: question.id }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: 'Не удалось удалить вопрос' }));
                }
            });
    };

    const questionType = useMemo(() => {
        return OPTIONS.find((option) => option.value === typeQuestion);
    }, [typeQuestion]);

    const questionContent = useCallback(() => {
        switch (typeQuestion) {
            case 'SHORT_TEXT':
                return <ShortText />;
            case 'LONG_TEXT':
                return <LongText />;
            case 'SINGLE_CHOICE':
                return <Choice options={question.answerOptions!} isEdit={isEditMode} type='radio' />;
            case 'MULTIPLE_CHOICE':
                return <Choice options={question.answerOptions!} isEdit={isEditMode} type='checkbox' />;
            default:
                return null;
        }
    }, [question, typeQuestion, isEditMode]);
    return (
        <div className={classNames(style.container, { [style.edit]: isEditMode })} onClick={onClick}>
            <section className={style.settings}>
                <Input
                    placeholder='Вопрос'
                    value={title}
                    onChange={(e) => {
                        setTitle(e);
                    }}
                    onBlur={updateQuestionTitleHandler}
                />
                <div className={style.button}>
                    <img src='/img.svg' alt='img' />
                </div>
                <Select
                    type='label'
                    value={questionType}
                    dataProvider={createStaticDataProvider(OPTIONS, 'Тип вопроса')}
                    name='area'
                    onChange={(e) => {
                        setTypeQuestion(e.value as QuestionType);
                    }}
                    onBlur={updateQuestionTypeHandler}
                />
            </section>

            <section className={style.actions}>
                <div>{questionContent()}</div>
                <div
                    className={classNames(style.actionsContent, {
                        [style.hidden]: !isEditMode,
                        [style.visible]: isEditMode,
                    })}
                >
                    <label className={style.mandatoryCheckbox}>
                        <Checkbox checked={mandatory} onChange={() => setMandatory(!mandatory)} />
                        Обязательный
                    </label>
                    <Button
                        mode='secondary'
                        type='button'
                        icon={<img src='/copy.svg' alt='Дублировать' />}
                        onClick={() => dispatch(duplicateQuestion({ id: question.id }))}
                    />
                    <Button
                        mode='secondary'
                        style='negative'
                        type='button'
                        icon={<img src='/trash.svg' alt='Удалить' />}
                        onClick={deleteQuestionHandler}
                    />
                </div>
            </section>
        </div>
    );
}
