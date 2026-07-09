import { Input } from '@hh.ru/magritte-ui-input';
import { TextArea } from '@hh.ru/magritte-ui-textarea';
import type { Survey } from '@/shared/types/Survey.type';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { useEffect, useState } from 'react';

import { updateSurvey } from '@/api/survey';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './SurveyDetail.module.css';

interface Props {
    survey: Survey;
}

export function SurveyDetail({ survey }: Props) {
    const [title, setTitle] = useState<string>(survey.title);
    const [description, setDescription] = useState<string>(survey.description ? survey.description : '');

    const dispatch = useAppDispatch();

    const updateTitleHandler = () => {
        if (title !== survey.title) {
            updateSurvey(survey.id, { title })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((error) => {
                    if (error.response) {
                        dispatch(setErrorMessage({ message: `Не удалось изменить название опроса` }));
                    }
                    setTitle(survey.title);
                });
        }
    };

    const updateDescriptionHandler = () => {
        if (description !== survey.description) {
            updateSurvey(survey.id, { description })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((error) => {
                    if (error.response) {
                        dispatch(setErrorMessage({ message: `Не удалось изменить описание опроса` }));
                    }
                    setTitle(survey.description ? survey.description : '');
                });
        }
    };

    useEffect(() => {
        return () => {
            updateTitleHandler();
            updateDescriptionHandler();
        };
    }, []);

    return (
        <div className={style.container}>
            <Input
                placeholder='Название формы'
                value={title}
                onChange={(value: string) => setTitle(value)}
                onBlur={updateTitleHandler}
            />

            <TextArea
                placeholder='Описание формы'
                data-qa='textarea'
                layout='fixed'
                resize='none'
                description='Описание формы - для чего она нужна, что в ней будет'
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                elevatePlaceholder={true}
                onBlur={updateDescriptionHandler}
            />
        </div>
    );
}
