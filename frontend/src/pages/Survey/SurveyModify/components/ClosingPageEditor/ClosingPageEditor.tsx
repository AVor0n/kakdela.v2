import { Button, Input, Text, Title } from '@hh.ru/magritte-ui';
import { useEffect, useState, type ChangeEvent } from 'react';
import {
    createClosingPage,
    deleteClosingPage,
    deleteClosingPageFile,
    deleteClosingPageImage,
    saveClosingPageFile,
    saveClosingPageImage,
    updateClosingPage,
} from '@/api/closingPage';
import { patchClosingPage, setClosingPage } from '@/entities/Survey/Survey.slice';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import type { ClosingPage } from '@/shared/types/Survey.type';
import { EditorInput } from '@/shared/ui/EditorInput/EditorInput';
import { ImageAttachmentControl } from '@/shared/ui/ImageAttachmentControl/ImageAttachmentControl';
import { hasHtmlText } from '@/shared/utils/html';
import style from './ClosingPageEditor.module.css';

const MAX_FILE_SIZE = 10 * 1024 * 1024;
const FORBIDDEN_FILE_TYPES = ['executable', 'x-msdownload', 'x-javascript', 'x-sh'];

type Props = {
    surveyId: string;
    closingPage: ClosingPage | null;
};

type PendingAction = 'create' | 'delete' | 'title' | 'description' | 'website' | 'image' | 'file';

function formatFileSize(size: number) {
    if (size < 1024) return `${size} Б`;
    if (size < 1024 * 1024) return `${Math.ceil(size / 1024)} КБ`;
    return `${(size / 1024 / 1024).toFixed(1)} МБ`;
}

function isValidWebsiteUrl(value: string) {
    try {
        const url = new URL(value);
        return url.protocol === 'http:' || url.protocol === 'https:';
    } catch {
        return false;
    }
}

export function ClosingPageEditor({ surveyId, closingPage }: Props) {
    const dispatch = useAppDispatch();
    const [title, setTitle] = useState(closingPage?.title ?? '');
    const [description, setDescription] = useState(closingPage?.description ?? '');
    const [websiteUrl, setWebsiteUrl] = useState(closingPage?.websiteUrl ?? '');
    const [fieldError, setFieldError] = useState<string | null>(null);
    const [pendingAction, setPendingAction] = useState<PendingAction | null>(null);

    useEffect(() => {
        setTitle(closingPage?.title ?? '');
        setDescription(closingPage?.description ?? '');
        setWebsiteUrl(closingPage?.websiteUrl ?? '');
    }, [closingPage]);

    const reportError = (message: string) => {
        dispatch(setErrorMessage({ message }));
    };

    const runAction = async <T,>(
        action: PendingAction,
        request: () => Promise<T>,
        onSuccess: (_result: T) => void,
        errorMessage: string,
        onError?: () => void,
    ) => {
        setPendingAction(action);
        try {
            onSuccess(await request());
        } catch {
            onError?.();
            reportError(errorMessage);
        } finally {
            setPendingAction(null);
        }
    };

    const createHandler = () =>
        runAction(
            'create',
            () =>
                createClosingPage(surveyId, {
                    title: '<p>Спасибо за ответ</p>',
                    description: '<p>Ваши ответы приняты.</p>',
                }),
            (createdClosingPage) => dispatch(setClosingPage({ closingPage: createdClosingPage })),
            'Не удалось создать завершающую страницу',
        );

    const deleteHandler = () =>
        runAction(
            'delete',
            () => deleteClosingPage(surveyId),
            () => dispatch(setClosingPage({ closingPage: null })),
            'Не удалось удалить завершающую страницу',
        );

    const saveTitle = () => {
        if (!closingPage || title === (closingPage.title ?? '')) return;
        if (!hasHtmlText(title)) {
            setFieldError('Заголовок нельзя оставить пустым');
            setTitle(closingPage.title ?? '');
            return;
        }
        if (title.length > 200) {
            setFieldError('Заголовок не должен быть длиннее 200 символов');
            return;
        }

        setFieldError(null);
        return runAction(
            'title',
            () => updateClosingPage(surveyId, { title }),
            (updatedClosingPage) => dispatch(setClosingPage({ closingPage: updatedClosingPage })),
            'Не удалось изменить заголовок завершающей страницы',
            () => setTitle(closingPage.title ?? ''),
        );
    };

    const saveDescription = () => {
        if (!closingPage || description === (closingPage.description ?? '')) return;
        if (description.length > 5000) {
            setFieldError('Описание не должно быть длиннее 5000 символов');
            return;
        }

        setFieldError(null);
        return runAction(
            'description',
            () => updateClosingPage(surveyId, { description }),
            (updatedClosingPage) => dispatch(setClosingPage({ closingPage: updatedClosingPage })),
            'Не удалось изменить описание завершающей страницы',
            () => setDescription(closingPage.description ?? ''),
        );
    };

    const saveWebsiteUrl = () => {
        if (!closingPage || websiteUrl === (closingPage.websiteUrl ?? '')) return;
        const normalizedUrl = websiteUrl.trim();

        if (!normalizedUrl && closingPage.websiteUrl) {
            setFieldError('Текущий API не позволяет удалить ссылку — её можно только заменить');
            setWebsiteUrl(closingPage.websiteUrl);
            return;
        }
        if (!normalizedUrl) return;
        if (normalizedUrl.length > 2000 || !isValidWebsiteUrl(normalizedUrl)) {
            setFieldError('Введите корректную ссылку, начинающуюся с http:// или https://');
            return;
        }

        setFieldError(null);
        return runAction(
            'website',
            () => updateClosingPage(surveyId, { websiteUrl: normalizedUrl }),
            (updatedClosingPage) => dispatch(setClosingPage({ closingPage: updatedClosingPage })),
            'Не удалось изменить ссылку завершающей страницы',
            () => setWebsiteUrl(closingPage.websiteUrl ?? ''),
        );
    };

    const imageChangeHandler = (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        event.target.value = '';
        if (!file || !closingPage) return;

        return runAction(
            'image',
            () => saveClosingPageImage(surveyId, file, Boolean(closingPage.attachmentUrl)),
            (response) => dispatch(patchClosingPage({ attachmentUrl: response.attachmentUrl })),
            'Не удалось загрузить изображение завершающей страницы',
        );
    };

    const deleteImageHandler = () =>
        runAction(
            'image',
            () => deleteClosingPageImage(surveyId),
            () => dispatch(patchClosingPage({ attachmentUrl: null })),
            'Не удалось удалить изображение завершающей страницы',
        );

    const fileChangeHandler = (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        event.target.value = '';
        if (!file || !closingPage) return;

        if (file.size === 0 || file.size > MAX_FILE_SIZE) {
            setFieldError('Размер файла должен быть больше 0 и не превышать 10 МБ');
            return;
        }
        if (FORBIDDEN_FILE_TYPES.some((type) => file.type.toLowerCase().includes(type))) {
            setFieldError('Исполняемые файлы загружать нельзя');
            return;
        }

        setFieldError(null);
        return runAction(
            'file',
            () => saveClosingPageFile(surveyId, file, Boolean(closingPage.file)),
            (uploadedFile) => dispatch(patchClosingPage({ file: uploadedFile })),
            'Не удалось загрузить файл завершающей страницы',
        );
    };

    const deleteFileHandler = () =>
        runAction(
            'file',
            () => deleteClosingPageFile(surveyId),
            () => dispatch(patchClosingPage({ file: null })),
            'Не удалось удалить файл завершающей страницы',
        );

    if (!closingPage) {
        return (
            <section className={style.container}>
                <Title Element='h2' size='small'>
                    Завершающая страница
                </Title>
                <Text typography='paragraph-2-regular' style='secondary'>
                    Добавьте сообщение, изображение, ссылку или файл, которые увидит пользователь после отправки
                    ответов.
                </Text>
                <Button mode='primary' style='accent' disabled={pendingAction !== null} onClick={createHandler}>
                    {pendingAction === 'create' ? 'Создаём...' : 'Настроить завершающую страницу'}
                </Button>
            </section>
        );
    }

    return (
        <section className={style.container}>
            <div className={style.header}>
                <Title Element='h2' size='small'>
                    Завершающая страница
                </Title>
                <Button mode='secondary' style='negative' disabled={pendingAction !== null} onClick={deleteHandler}>
                    Удалить страницу
                </Button>
            </div>

            {closingPage.attachmentUrl && (
                <img className={style.image} src={closingPage.attachmentUrl} alt='Изображение завершающей страницы' />
            )}

            <div className={style.titleSettings}>
                <EditorInput
                    placeholder='Заголовок'
                    value={title}
                    onChange={setTitle}
                    onBlur={() => void saveTitle()}
                    isTextColor
                />
                <ImageAttachmentControl
                    inputId={`closing-page-image-${surveyId}`}
                    hasImage={Boolean(closingPage.attachmentUrl)}
                    disabled={pendingAction !== null}
                    onChange={imageChangeHandler}
                    onDelete={deleteImageHandler}
                />
            </div>
            <EditorInput
                placeholder='Описание'
                value={description}
                onChange={setDescription}
                onBlur={() => void saveDescription()}
                isTextColor
                isMarkColor
                isHeading
            />
            <Input
                placeholder='https://example.com'
                size='large'
                value={websiteUrl}
                disabled={pendingAction !== null}
                onChange={setWebsiteUrl}
                onBlur={() => void saveWebsiteUrl()}
            />

            <div className={style.fileSection}>
                {closingPage.file ? (
                    <div className={style.fileInfo}>
                        <div>
                            <Text typography='paragraph-2-regular' style='primary'>
                                {closingPage.file.fileName}
                            </Text>
                            <Text typography='paragraph-2-regular' style='secondary'>
                                {formatFileSize(closingPage.file.fileSize)}
                            </Text>
                        </div>
                        <Button
                            mode='secondary'
                            style='negative'
                            disabled={pendingAction !== null}
                            onClick={deleteFileHandler}
                        >
                            Удалить файл
                        </Button>
                    </div>
                ) : (
                    <Text typography='paragraph-2-regular' style='secondary'>
                        Файл не прикреплён. Максимальный размер — 10 МБ.
                    </Text>
                )}
                <input
                    className={style.hiddenInput}
                    id={`closing-page-file-${surveyId}`}
                    type='file'
                    disabled={pendingAction !== null}
                    onChange={fileChangeHandler}
                />
                <label className={style.fileButton} htmlFor={`closing-page-file-${surveyId}`}>
                    {closingPage.file ? 'Заменить файл' : 'Добавить файл'}
                </label>
            </div>

            {fieldError && (
                <Text typography='paragraph-2-regular' style='negative'>
                    {fieldError}
                </Text>
            )}
        </section>
    );
}
