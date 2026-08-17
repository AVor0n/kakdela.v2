import type { Survey, Template } from '@/shared/types/Survey.type';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { useEffect, useState, type ChangeEvent } from 'react';

import { updateSurvey } from '@/api/survey';
import { deleteOpeningPageImage, saveOpeningPageImage } from '@/api/openingPage';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './SurveyDetail.module.css';
import { EditorInput } from '@/shared/ui/EditorInput/EditorInput';
import { ImageAttachmentControl } from '@/shared/ui/ImageAttachmentControl/ImageAttachmentControl';

interface Props {
    item: Pick<Survey | Template, 'id' | 'title' | 'description'>;
    attachmentUrl?: string | null;
    canEditImage?: boolean;
    replaceImageWithoutUrl?: boolean;
    onAttachmentUrlChange: (_attachmentUrl: string | null) => void;
}

export function SurveyDetail({
    item,
    attachmentUrl,
    canEditImage = false,
    replaceImageWithoutUrl = false,
    onAttachmentUrlChange,
}: Props) {
    const [title, setTitle] = useState<string>(item.title);
    const [description, setDescription] = useState<string>(item.description ? item.description : '');
    const [isImagePending, setIsImagePending] = useState(false);

    const dispatch = useAppDispatch();
    const imageChangeHandler = async (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        event.target.value = '';
        if (!file || !canEditImage) return;

        setIsImagePending(true);
        try {
            const response = await saveOpeningPageImage(
                item.id,
                file,
                replaceImageWithoutUrl || Boolean(attachmentUrl),
            );
            onAttachmentUrlChange(response.attachmentUrl);
        } catch {
            dispatch(setErrorMessage({ message: 'Не удалось загрузить изображение приветственной страницы' }));
        } finally {
            setIsImagePending(false);
        }
    };

    const deleteImageHandler = async () => {
        if (!canEditImage) return;

        setIsImagePending(true);
        try {
            await deleteOpeningPageImage(item.id);
            onAttachmentUrlChange(null);
        } catch {
            dispatch(setErrorMessage({ message: 'Не удалось удалить изображение приветственной страницы' }));
        } finally {
            setIsImagePending(false);
        }
    };

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
            {attachmentUrl && (
                <img className={style.image} src={attachmentUrl} alt='Изображение приветственной страницы' />
            )}

            <div className={style.fieldsWithImageControl}>
                <div className={style.fields}>
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
                {canEditImage && (
                    <ImageAttachmentControl
                        inputId={`opening-page-image-${item.id}`}
                        hasImage={Boolean(attachmentUrl)}
                        disabled={isImagePending}
                        onChange={imageChangeHandler}
                        onDelete={deleteImageHandler}
                    />
                )}
            </div>
        </div>
    );
}
