import { Input } from '@hh.ru/magritte-ui-input';
import { TextArea } from '@hh.ru/magritte-ui-textarea';
import type { Survey } from '@/shared/types/Survey.type';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import style from './SurveyDetail.module.css';
interface Props {
    survey: Survey;
}

export function SurveyDetail({ survey }: Props) {
    const dispatch = useAppDispatch();
    return (
        <div className={style.container}>
            <Input
                placeholder='Название формы'
                value={survey!.title}
                onChange={(value: string) => dispatch(setSelectedSurvey({ survey: { ...survey, title: value } }))}
            />

            <TextArea
                placeholder='Описание формы'
                data-qa='textarea'
                layout='fixed'
                resize='none'
                description='Описание формы - для чего она нужна, что в ней будет'
                value={survey!.description ? survey.description : ''}
                onChange={(e) => dispatch(setSelectedSurvey({ survey: { ...survey, description: e.target.value } }))}
                elevatePlaceholder={true}
            />
        </div>
    );
}
