import { Button } from '@hh.ru/magritte-ui';
import type { Survey, SurveyPublic } from '@/shared/types/Survey.type';
import { formatDate } from '@/shared/utils/date';
import { SurveyFlowPage } from '../SurveyFlowPage/SurveyFlowPage';
import style from './WelcomePageView.module.css';

type Props = {
    survey: Survey | SurveyPublic;
    onStart: () => void;
    isStarting?: boolean;
    startError?: string | null;
};

export function WelcomePageView({ survey, onStart, isStarting = false, startError }: Props) {
    const deadline = survey.expireAtAtTargetTimezone ?? survey.expireAt;
    const authorName = survey.author.login || survey.author.email;

    return (
        <SurveyFlowPage title={survey.title} description={survey.description}>
            <dl className={style.metadata}>
                <div className={style.metadataRow}>
                    <dt>Автор</dt>
                    <dd>{authorName}</dd>
                </div>
                {'createdAt' in survey && (
                    <div className={style.metadataRow}>
                        <dt>Дата создания</dt>
                        <dd>{formatDate(survey.createdAt)}</dd>
                    </div>
                )}
                {deadline && (
                    <div className={style.metadataRow}>
                        <dt>Дедлайн</dt>
                        <dd>{formatDate(deadline)}</dd>
                    </div>
                )}
            </dl>
            <div className={style.startAction}>
                <Button mode='primary' style='accent' disabled={isStarting} onClick={onStart}>
                    {isStarting ? 'Загружаем...' : 'Перейти к опросу'}
                </Button>
                {startError && <p>{startError}</p>}
            </div>
        </SurveyFlowPage>
    );
}
