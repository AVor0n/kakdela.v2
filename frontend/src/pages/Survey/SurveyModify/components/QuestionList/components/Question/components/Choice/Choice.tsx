import { addAnotherOption, addQuestionOptions, deleteOption, setOptionValue } from '@/entities/Survey/Survey.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { Button, Checkbox, Link, Radio } from '@hh.ru/magritte-ui';
import style from './Choice.module.css';
interface Props {
    options: string[];
    type: 'radio' | 'checkbox';
    isEdit: boolean;
}

export function Choice({ options, type, isEdit }: Props) {
    const dispatch = useAppDispatch();
    return (
        <div className={style.container}>
            {options.map((option, index) => (
                <div key={index} className={style.optionContent}>
                    <label className={style.option}>
                        {type === 'checkbox' ? (
                            <Checkbox name='multiple_choice' checked={false} onChange={() => {}} />
                        ) : (
                            <Radio name='single_choice' checked={false} onChange={() => {}} />
                        )}
                        {option !== 'Другое' ? (
                            <input
                                className={style.input}
                                value={option}
                                onChange={(e) => dispatch(setOptionValue({ value: e.target.value, index }))}
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
                            onClick={() => dispatch(deleteOption({ removeIndex: index }))}
                            mode='secondary'
                            style='negative'
                            icon={<img src='/X.svg' alt='X' />}
                            size='small'
                            className=''
                        />
                    )}
                </div>
            ))}
            {isEdit && (
                <div className={style.add}>
                    <p className={style.actions}>
                        <Link
                            Element='button'
                            mode='secondary'
                            style='accent'
                            onClick={() => dispatch(addQuestionOptions())}
                        >
                            Добавить ответ
                        </Link>
                        <span>или</span>
                        <Link
                            Element='button'
                            mode='secondary'
                            style='accent'
                            onClick={() => dispatch(addAnotherOption())}
                        >
                            Добавить вариант "Другое"
                        </Link>
                    </p>
                </div>
            )}
        </div>
    );
}
