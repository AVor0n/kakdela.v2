import { Radio } from '@hh.ru/magritte-ui';

interface Props {
    options: string[];
}

export function SingleChoice({ options }: Props) {
    return (
        <div>
            {options.map((option, index) => (
                <div key={index}>
                    <Radio name='single_choice' checked={false} />
                    <span>{option}</span>
                </div>
            ))}
        </div>
    );
}
