import type { Question } from '@/shared/types/Question.type';
import { Button, createStaticDataProvider, Input, Select } from '@hh.ru/magritte-ui';
import { useCallback, useState } from 'react';
import './Question.css';
import { ShortText } from './components/ShortText/ShortText';
import { LongText } from './components/LongText/LongText';
import { SingleChoice } from './components/SingleChoice/SingleChoice';
import { MultipleChoice } from './components/MultipleChoice/MultipleChoice';
interface Props {
    question: Question;
    isEditMode?: boolean;
    onClick?: () => void;
}

export function Question({ question, onClick, isEditMode = false }: Props) {
    const [value, setValue] = useState(question.title);

    const OPTIONS = [
        { id: 'SHORT_TEXT', value: 'Короткий текст', disabled: false },
        { id: 'LONG_TEXT', value: 'Длинный текст', disabled: false },
        { id: 'SINGLE_CHOICE', value: 'Один из списка', disabled: false },
        { id: 'MULTIPLE_CHOICE', value: 'Несколько из списка', disabled: false },
    ];
    const [questionType, setQuestionType] = useState(
        OPTIONS.find((option) => option.id === question.type) || OPTIONS[0],
    );

    const questionContent = useCallback(() => {
        switch (question.type) {
            case 'SHORT_TEXT':
                return <ShortText />;
            case 'LONG_TEXT':
                return <LongText />;
            case 'SINGLE_CHOICE':
                return <SingleChoice options={question.answerOptions!} />;
            case 'MULTIPLE_CHOICE':
                return <MultipleChoice options={question.answerOptions!} />;
            default:
                return null;
        }
    }, [question.type]);

    return (
        <div className={isEditMode ? 'question editing' : 'question'} onClick={onClick}>
            <section className='question__settings'>
                <Input
                    placeholder='Вопрос'
                    value={value}
                    onChange={(e) => setValue(e)}
                    className='question__settings_title'
                />
                <div className='question__settings_img'>img</div>
                <Select
                    type='label'
                    value={questionType}
                    dataProvider={createStaticDataProvider(OPTIONS, 'Тип вопроса')}
                    name='questionType'
                    onChange={(e) => {
                        setQuestionType(e);
                    }}
                    pickerDesktopType='drop'
                    widthEqualToActivator
                ></Select>
            </section>

            <section className='question__content'>
                <div className='question__content_options'>{questionContent()}</div>
                <div className='question__content_actions'>
                    <Button mode='secondary' type='button'>
                        Обязательный
                    </Button>
                    <Button mode='secondary' type='button'>
                        Дублировать
                    </Button>
                    <Button mode='secondary' style='negative' type='button'>
                        Удлить
                    </Button>
                </div>
            </section>
        </div>
    );
}
