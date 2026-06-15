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
import './Question.css';
import { ShortText } from './components/ShortText/ShortText';
import { LongText } from './components/LongText/LongText';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setMandatory, updateQuestionTitle, updateQuestionType } from '@/entities/Survey/Survey.slice';
import { Choice } from './components/Choice/Choice';
interface Props {
    question: Question;
    isEditMode?: boolean;
    onClick?: () => void;
}

export function Question({ question, onClick, isEditMode = false }: Props) {
    const dispatch = useAppDispatch();

    const OPTIONS: StaticDataFetcherItem[] = [
        { value: 'SHORT_TEXT', text: 'Короткий текст' },
        { value: 'LONG_TEXT', text: 'Длинный текст' },
        { value: 'SINGLE_CHOICE', text: 'Один из списка' },
        { value: 'MULTIPLE_CHOICE', text: 'Несколько из списка' },
    ];
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
                return <Choice options={question.answerOptions!} type='radio' />;
            case 'MULTIPLE_CHOICE':
                return <Choice options={question.answerOptions!} type='checkbox' />;
            default:
                return null;
        }
    }, [question]);

    return (
        <div className={isEditMode ? 'question editing' : 'question'} onClick={onClick}>
            {question.mandatory ? <span className='mandatory'>*</span> : null}
            <section className='question__settings'>
                <Input
                    placeholder='Вопрос'
                    value={question.title}
                    onChange={(e) => {
                        dispatch(updateQuestionTitle({ id: question.id, title: e }));
                    }}
                    className='question__settings_title'
                />
                <div className='question__settings_img'>img</div>
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
                    triggerProps={{
                        'aria-label': 'Выбор языка',
                    }}
                />
            </section>

            <section className='question__content'>
                <div className='question__content_options'>{questionContent()}</div>
                <div className='question__content_actions'>
                    <label className='mandatory__checkbox'>
                        <Checkbox checked={question.mandatory} onChange={() => dispatch(setMandatory())} />
                        Обязательный
                    </label>
                    <Button mode='secondary' type='button'>
                        Дублировать
                    </Button>
                    <Button mode='secondary' style='negative' type='button'>
                        Удалить
                    </Button>
                </div>
            </section>
        </div>
    );
}
