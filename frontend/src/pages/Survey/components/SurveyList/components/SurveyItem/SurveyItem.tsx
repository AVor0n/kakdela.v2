import { Button, Text } from '@hh.ru/magritte-ui';
import type { SurveyListItem } from '@/shared/types/Survey.type';
import style from './SuveyItem.module.css';
import { cloneSurvey, deleteSurvey } from '@/api/survey';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { addSurvey, deleteSurvey as deleteSurveyState } from '@/entities/Survey/Survey.slice';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { useState } from 'react';
import { CheckOutlinedSize24, CrossOutlinedSize24 } from '@hh.ru/magritte-ui/icon';
type SurveyItemProps = {
    survey: SurveyListItem;
    onClick: () => void;
};

export function SurveyItem({ survey, onClick }: SurveyItemProps) {
    const [isDeleteSurvey, setIsDeleteSurvey] = useState<boolean>();
    const dispatch = useAppDispatch();
    const deleteSurveyHandler = () => {
        deleteSurvey(survey.id)
            .then(() => {
                dispatch(deleteSurveyState({ surveyId: survey.id }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(
                        setErrorMessage({
                            message: 'Не удалось удалить опрос',
                        }),
                    );
                }
            });
    };
    const duplicateSurvey = () => {
        cloneSurvey(survey.id)
            .then((data) => {
                dispatch(addSurvey({ survey: data }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(
                        setErrorMessage({
                            message: 'Не удалось копировать опрос',
                        }),
                    );
                }
            });
    };
    return (
        <div className={style.container}>
            <button type='button' onClick={onClick} className={style.details}>
                <div className={style.detail}>
                    <Text typography='title-4-semibold' style='primary'>
                        {survey.title}
                    </Text>
                    {survey.description && <p className={style.description}>{survey.description}</p>}
                </div>
            </button>
            <div className={style.actions}>
                <Button
                    mode='secondary'
                    type='button'
                    icon={<img src='/copy.svg' alt='Дублировать' />}
                    onClick={duplicateSurvey}
                />
                {isDeleteSurvey ? (
                    <div className={style.deleteButtons}>
                        <Button
                            mode='secondary'
                            style='neutral'
                            icon={<CrossOutlinedSize24 />}
                            onClick={() => setIsDeleteSurvey(false)}
                        />
                        <Button
                            mode='secondary'
                            style='accent'
                            icon={<CheckOutlinedSize24 />}
                            onClick={deleteSurveyHandler}
                        />
                    </div>
                ) : (
                    <Button
                        mode='secondary'
                        style='negative'
                        type='button'
                        icon={<img src='/trash.svg' alt='Удалить' />}
                        onClick={() => setIsDeleteSurvey(true)}
                    />
                )}
            </div>
        </div>
    );
}
