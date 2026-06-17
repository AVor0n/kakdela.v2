import type { Question, QuestionType } from '@/shared/types/Question.type';
import {
    Button,
    Checkbox,
    createStaticDataProvider,
    Input,
    Select,
    type StaticDataFetcherItem,
} from '@hh.ru/magritte-ui';
import { useCallback, useMemo } from 'react';
import { ShortText } from './components/ShortText/ShortText';
import { LongText } from './components/LongText/LongText';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import {
    deleteQuestion,
    duplicateQuestion,
    setMandatory,
    updateQuestionTitle,
    updateQuestionType,
} from '@/entities/Survey/Survey.slice';
import { Choice } from './components/Choice/Choice';
import classNames from 'classnames';
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
    const dispatch = useAppDispatch();

    const questionType = useMemo(() => {
        return OPTIONS.find((option) => option.value === question.type);
    }, [question.type]);

    const questionContent = useCallback(() => {
        switch (question.type) {
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
    }, [question, isEditMode]);
    // isEditMode ? [style.question__edit, style.question].join(' ') : style.question
    return (
        <div className={classNames(style.container, { [style.edit]: isEditMode })} onClick={onClick}>
            <section className={style.settings}>
                <Input
                    placeholder='Вопрос'
                    value={question.title}
                    onChange={(e) => {
                        dispatch(updateQuestionTitle({ id: question.id, title: e }));
                    }}
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
                        dispatch(
                            updateQuestionType({
                                type: e.value as QuestionType,
                            }),
                        );
                    }}
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
                        <Checkbox checked={question.mandatory} onChange={() => dispatch(setMandatory())} />
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
                        onClick={() => dispatch(deleteQuestion({ id: question.id }))}
                    />
                </div>
            </section>
        </div>
    );
}
