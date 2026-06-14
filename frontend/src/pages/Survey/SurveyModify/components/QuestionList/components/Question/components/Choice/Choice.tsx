import { addQuestionOptions, deleteOption } from '@/entities/Survey/Survey.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { Button, Checkbox, Radio } from '@hh.ru/magritte-ui';
import './Choice.css';
interface Props {
    options: string[];
    type: 'radio' | 'checkbox';
}

export function Choice({ options, type }: Props) {
    const dispatch = useAppDispatch();
    return (
        <div className='option__list'>
            {options.map((option, index) => (
                <div key={index} className='option__container'>
                    <div className='option'>
                        {type === 'checkbox' ? <Checkbox name='multiple_choice' /> : <Radio name='single_choice' />}
                        <span>{option}</span>
                    </div>
                    <Button
                        mode='secondary'
                        size='small'
                        onClick={() => dispatch(deleteOption({ removeValue: option }))}
                    >
                        x
                    </Button>
                </div>
            ))}
            <div className='question__content_add-option'>
                {type === 'checkbox' ? <Checkbox name='multiple_choice' /> : <Radio name='single_choice' />}
                <span>
                    {' '}
                    <button onClick={() => dispatch(addQuestionOptions())}>Добавить ответ</button> или Добавить вариант
                    "Другое"
                </span>
            </div>
        </div>
    );
}
