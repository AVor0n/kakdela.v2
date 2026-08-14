import type { Survey, Template } from '@/shared/types/Survey.type';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { useEffect, useState } from 'react';

import { updateSurvey } from '@/api/survey';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './SurveyDetail.module.css';
import { EditorInput } from '@/shared/ui/EditorInput/EditorInput';

interface Props {
    item: Pick<Survey | Template, 'id' | 'title' | 'description'>;
}

export function SurveyDetail({ item }: Props) {
    const [title, setTitle] = useState<string>(item.title);
    const [description, setDescription] = useState<string>(item.description ? item.description : '');

    const dispatch = useAppDispatch();

    const updateTitleHandler = () => {
        if (title !== item.title) {
            updateSurvey(item.id, { title })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((error) => {
                    if (error.response) {
                        dispatch(setErrorMessage({ message: `Не удалось изменить название опроса` }));
                    }
                    setTitle(item.title);
                });
        }
    };

    const updateDescriptionHandler = () => {
        if (description !== item.description) {
            updateSurvey(item.id, { description })
                .then((data) => {
                    dispatch(setSelectedSurvey({ survey: data }));
                })
                .catch((error) => {
                    if (error.response) {
                        dispatch(setErrorMessage({ message: `Не удалось изменить описание опроса` }));
                    }
                    setDescription(item.description ? item.description : '');
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
            <EditorInput
                placeholder='Название формы'
                value={title}
                onBlur={updateTitleHandler}
                onChange={setTitle}
                isTextColor
            />

            <EditorInput
                placeholder='Описание формы'
                value={description}
                onBlur={updateDescriptionHandler}
                onChange={setDescription}
                isTextColor
                isMarkColor
                isHeading
                disabled={true}
            />
        </div>
    );
}
