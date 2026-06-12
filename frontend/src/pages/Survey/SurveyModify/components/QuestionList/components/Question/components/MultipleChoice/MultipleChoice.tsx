import { Checkbox } from '@hh.ru/magritte-ui';

interface Props {
    options: string[];
}

export function MultipleChoice({ options }: Props) {
    return (
        <div>
            {options.map((option, index) => (
                <div key={index}>
                    <Checkbox name='multiple_choice' checked={false} />
                    <span>{option}</span>
                </div>
            ))}
        </div>
    );
}
