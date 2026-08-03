import { Button, Text } from '@hh.ru/magritte-ui';
import { useState } from 'react';
import { getClosingPageFileUrl } from '@/api/closingPage';
import type { ClosingPage } from '@/shared/types/Survey.type';
import { downloadBlob } from '@/shared/utils/download';
import { SurveyFlowPage } from '../SurveyFlowPage/SurveyFlowPage';
import style from './ClosingPageView.module.css';

type Props = {
    surveyId: string;
    closingPage: ClosingPage | null;
    onBack?: () => void;
};

async function downloadFile(url: string, fileName: string) {
    const response = await fetch(url);
    if (!response.ok) throw new Error('Download failed');

    downloadBlob(await response.blob(), fileName);
}

function openFile(url: string) {
    const link = document.createElement('a');
    link.href = url;
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
    document.body.appendChild(link);
    link.click();
    link.remove();
}

export function ClosingPageView({ surveyId, closingPage, onBack }: Props) {
    const [isDownloading, setIsDownloading] = useState(false);
    const [downloadError, setDownloadError] = useState<string | null>(null);

    const downloadHandler = async () => {
        if (!closingPage?.file) return;

        setIsDownloading(true);
        setDownloadError(null);
        let attachmentUrl: string | null = null;

        try {
            attachmentUrl = await getClosingPageFileUrl(surveyId);
            await downloadFile(attachmentUrl, closingPage.file.fileName);
        } catch {
            if (attachmentUrl) {
                openFile(attachmentUrl);
            } else {
                setDownloadError('Не удалось получить файл. Попробуйте ещё раз.');
            }
        } finally {
            setIsDownloading(false);
        }
    };

    const image = closingPage?.attachmentUrl ? (
        <img className={style.image} src={closingPage.attachmentUrl} alt='Изображение завершающей страницы' />
    ) : undefined;

    return (
        <SurveyFlowPage
            title={closingPage?.title ?? null}
            description={closingPage?.description ?? null}
            fallbackTitle='Спасибо за ответ'
            fallbackDescription='Ваши ответы приняты.'
            media={image}
        >
            {closingPage?.websiteUrl && (
                <a className={style.website} href={closingPage.websiteUrl} target='_blank' rel='noopener noreferrer'>
                    Перейти на сайт
                </a>
            )}

            {closingPage?.file && (
                <div className={style.file}>
                    <div>
                        <Text typography='paragraph-2-regular' style='primary'>
                            {closingPage.file.fileName}
                        </Text>
                    </div>
                    <Button
                        mode='primary'
                        style='accent'
                        disabled={isDownloading}
                        onClick={() => void downloadHandler()}
                    >
                        {isDownloading ? 'Скачиваем...' : 'Скачать файл'}
                    </Button>
                </div>
            )}

            {downloadError && (
                <Text typography='paragraph-2-regular' style='negative'>
                    {downloadError}
                </Text>
            )}

            {onBack && (
                <Button mode='secondary' style='accent' onClick={onBack}>
                    Вернуться к вопросам
                </Button>
            )}
        </SurveyFlowPage>
    );
}
