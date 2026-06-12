import { Input } from '@hh.ru/magritte-ui-input';
import { TextArea } from '@hh.ru/magritte-ui-textarea';
import type { Survey } from '@/shared/types/Survey.type';
import type { Dispatch, SetStateAction } from 'react';
import './SurveyDetail.css';
interface Props {
    survey: Survey;
    setSurvey: Dispatch<SetStateAction<Survey>>;
}

export function SurveyDetail({ survey, setSurvey }: Props) {
    return (
        <div className='survey__detail'>
            <Input
                placeholder='Название формы'
                value={survey!.title}
                onChange={(value: string) => setSurvey((prev: Survey) => (prev ? { ...prev, title: value } : prev))}
            />

            <TextArea
                placeholder='Описание формы'
                data-qa='textarea'
                layout='fixed'
                resize='none'
                description='Описание формы - для чего она нужна, что в ней будет'
                value={survey!.description!}
                onChange={(e) => setSurvey((prev: Survey) => (prev ? { ...prev, description: e.target.value } : prev))}
                elevatePlaceholder={true}
            />
        </div>
    );
}
