import { addQuestionOptions, deleteOption } from '@/entities/Survey/Survey.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { Button, Checkbox, Link, Radio } from '@hh.ru/magritte-ui';
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
                    <label className='option'>
                        {type === 'checkbox' ? (
                            <Checkbox name='multiple_choice' checked={false} onChange={() => {}} />
                        ) : (
                            <Radio name='single_choice' checked={false} onChange={() => {}} />
                        )}
                        <span>{option}</span>
                    </label>
                    <Button
                        mode='secondary'
                        size='small'
                        onClick={() => dispatch(deleteOption({ removeValue: option }))}
                    >
                        x
                    </Button>
                </div>
            ))}
            <div className='question__content_add_option'>
                {type === 'checkbox' ? <Checkbox name='multiple_choice' /> : <Radio name='single_choice' />}
                <span className='add_option'>
                    {' '}
                    <Link Element='button' onClick={() => dispatch(addQuestionOptions())}>
                        Добавить ответ
                    </Link>{' '}
                    или{' '}
                    <Link Element='button' onClick={() => dispatch(addQuestionOptions())}>
                        Добавить вариант "Другое"
                    </Link>
                </span>
            </div>
        </div>
    );
}
