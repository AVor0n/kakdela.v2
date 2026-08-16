import type { TemplateListItem } from '@/shared/types/Survey.type';
import style from './TemplateItem.module.css';
import { formatDate } from '@/shared/utils/date';
import { useState } from 'react';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { createSurveyFromTemplate, deleteTemplate } from '@/api/template';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { htmlToText } from '@/shared/utils/html';
import { CheckOutlinedSize24, CrossOutlinedSize24, DotFilledSize16 } from '@hh.ru/magritte-ui/icon';
import { HTMLRender } from '@/shared/ui/HTMLRender/HTMLRender';
import { Button, Text } from '@hh.ru/magritte-ui';
import { addSurvey } from '@/entities/Survey/Survey.slice';
import { useNavigate } from 'react-router-dom';
import { routes } from '@/app/routes';
import { removeTemplate } from '@/entities/Template/Template.slice';
interface Props {
    template: TemplateListItem;
    onClick: () => void;
}

export function TemplateItem({ template, onClick }: Props) {
    const [isDeleteSurvey, setIsDeleteSurvey] = useState<boolean>();
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const deleteTemplateHandler = () => {
        deleteTemplate(template.id)
            .then(() => {
                dispatch(removeTemplate({ templateId: template.id }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(
                        setErrorMessage({
                            message: 'Не удалось удалить шаблон',
                        }),
                    );
                }
            });
    };
    const createSurveyHandler = () => {
        createSurveyFromTemplate(template.id)
            .then((data) => {
                dispatch(addSurvey({ survey: data }));
                navigate(routes.surveyQuestions(data.id));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(
                        setErrorMessage({
                            message: 'Не удалось создать опрос из шаблона',
                        }),
                    );
                }
            });
    };
    return (
        <div className={style.container}>
            <button className={style.details} onClick={onClick}>
                <div className={style.detail}>
                    <Text typography='title-4-semibold' style='primary'>
                        {htmlToText(template.title)}
                    </Text>
                    <div className={style.info}>
                        {template.published ? <p>Опубликован</p> : <p>Не опубликован</p>}
                        <DotFilledSize16 fill='#777777' color='#777777' />
                        <p>Дата создания: {formatDate(template.createdAt)}</p>
                    </div>
                    {template.description && <HTMLRender html={template.description} className={style.description} />}
                </div>
            </button>
            <div className={style.actions}>
                <Button mode='secondary' type='button' onClick={createSurveyHandler}>
                    Создать опрос
                </Button>
                {isDeleteSurvey ? (
                    <div className={style.deleteButtons}>
                        <Button
                            mode='secondary'
                            style='accent'
                            icon={<CheckOutlinedSize24 />}
                            onClick={deleteTemplateHandler}
                        />
                        <Button
                            mode='secondary'
                            style='neutral'
                            icon={<CrossOutlinedSize24 />}
                            onClick={() => setIsDeleteSurvey(false)}
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
