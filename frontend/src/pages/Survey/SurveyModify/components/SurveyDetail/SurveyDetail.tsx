import { Input } from '@hh.ru/magritte-ui-input';
import { TextArea } from '@hh.ru/magritte-ui-textarea';
import type { Survey } from '@/shared/types/Survey.type';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import style from './SurveyDetail.module.css';
import { useEffect, useState } from 'react';
import { useDebounce } from '@/hooks/useDebounce';
import { updateSurvey } from '@/api/survey';
import type { Error } from '@/shared/types/Error.type';
import { ErrorBlock } from '../ErrorBlock/ErrorBlock';
interface Props {
    survey: Survey;
}

export function SurveyDetail({ survey }: Props) {
    const [title, setTitle] = useState<string>(survey.title);
    const [description, setDescription] = useState<string>(survey.description ? survey.description : '');
    const [error, setError] = useState<Error | null>(null);
    const debouncedTitle = useDebounce(title, 2000);
    const debouncedDescription = useDebounce(description, 2000);
    const dispatch = useAppDispatch();

    useEffect(() => {
        if (debouncedTitle !== survey.title) {
            updateSurvey(survey.id, { title })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((error) => {
                    if (error.response) {
                        setError(error.response.data);
                    }
                    setTitle(survey.title);
                });
        }
    }, [debouncedTitle]);

    useEffect(() => {
        if (debouncedDescription !== survey.description) {
            updateSurvey(survey.id, { description })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((error) => {
                    if (error.response) {
                        setError(error.response.data);
                    }
                    setTitle(survey.description ? survey.description : '');
                });
        }
    }, [debouncedDescription]);

    return (
        <div className={style.container}>
            {error && <ErrorBlock error={error} setError={setError} />}
            <Input placeholder='Название формы' value={title} onChange={(value: string) => setTitle(value)} />

            <TextArea
                placeholder='Описание формы'
                data-qa='textarea'
                layout='fixed'
                resize='none'
                description='Описание формы - для чего она нужна, что в ней будет'
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                elevatePlaceholder={true}
            />
        </div>
    );
}
