import { addQuestionOptions } from '@/entities/Survey/Survey.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { Checkbox, Link, Radio } from '@hh.ru/magritte-ui';
import style from './Choice.module.css';
import { addAnswerOption } from '@/api/answer-option';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useState } from 'react';
import type { Error } from '@/shared/types/Error.type';
import { ErrorBlock } from '@/pages/Survey/SurveyModify/components/ErrorBlock/ErrorBlock';
import type { AnswerOption } from '@/shared/types/Question.type';
import { Option } from './components/Option/Option';
interface Props {
    options: AnswerOption[];
    type: 'radio' | 'checkbox';
    isEdit: boolean;
}

export function Choice({ options, type, isEdit }: Props) {
    const { selectedSurvey, selectedQuestion } = useAppSelector((state) => state.survey);
    const [error, setError] = useState<Error | null>(null);
    const dispatch = useAppDispatch();

    const createAnswerOptionHandler = () => {
        if (!selectedSurvey || !selectedQuestion) return;
        let serialNumber = 1;
        if (options.length !== 0) {
            const lastAnswerOptionSerialNumber = options[options.length - 1].serialNumber;
            serialNumber = lastAnswerOptionSerialNumber + 1;
        }

        addAnswerOption(selectedQuestion.id, {
            answerOptionText: `Вопрос ${serialNumber}`,
            serialNumber: serialNumber,
        })
            .then((data) => {
                dispatch(addQuestionOptions({ answerOption: data }));
            })
            .catch((err) => {
                if (err.response) {
                    setError(err.response.data);
                }
            });
    };

    const createAnotherOptionHandler = () => {
        if (!selectedSurvey || !selectedQuestion) return;
        let serialNumber = 1;
        if (options.length === 0) {
            const lastAnswerOptionSerialNumber = options[options.length - 1].serialNumber;
            serialNumber = lastAnswerOptionSerialNumber + 1;
        }

        addAnswerOption(selectedQuestion.id, {
            answerOptionText: 'Другое',
            serialNumber: serialNumber,
        })
            .then((data) => {
                dispatch(addQuestionOptions({ answerOption: data }));
            })
            .catch((err) => {
                if (err.response) {
                    setError(err.response.data);
                }
            });
    };

    return (
        <div className={style.container}>
            {error && <ErrorBlock error={error} setError={setError} />}
            {options.map((option) => (
                <Option option={option} isEdit={isEdit} key={option.id}>
                    {type === 'checkbox' ? (
                        <Checkbox name='multiple_choice' checked={false} onChange={() => {}} />
                    ) : (
                        <Radio name='single_choice' checked={false} onChange={() => {}} />
                    )}
                </Option>
            ))}
            {isEdit && (
                <div className={style.add}>
                    <div className={style.actions}>
                        <Link Element='button' mode='secondary' style='accent' onClick={createAnswerOptionHandler}>
                            Добавить ответ
                        </Link>
                        <span>или</span>
                        <Link Element='button' mode='secondary' style='accent' onClick={createAnotherOptionHandler}>
                            Добавить вариант "Другое"
                        </Link>
                    </div>
                </div>
            )}
        </div>
    );
}
