import { useEffect, useState } from 'react';
import type { Question } from '@/shared/types/Question.type';
import type { ClosingPage, Page, Survey, SurveyPageShort, SurveyPublic } from '@/shared/types/Survey.type';
import { Button, Text } from '@hh.ru/magritte-ui';
import { Link } from 'react-router-dom';
import { routes } from '@/app/routes';
import { completeSurveyResponse } from '@/api/surveyResponses';
import surveyDetailStyle from '@/pages/Survey/SurveyModify/components/SurveyDetail/SurveyDetail.module.css';
import questionStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/Question.module.css';
import choiceStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/Choice/Choice.module.css';
import style from './SurveyRunner.module.css';
import { useSurveyResponseSync } from './useSurveyResponseSync';
import { HTMLRender } from '@/shared/ui/HTMLRender/HTMLRender';
import { ClosingPageView } from '../ClosingPageView/ClosingPageView';
import { WelcomePageView } from '../WelcomePageView/WelcomePageView';
import { getClosingPage } from '@/api/closingPage';
import { getApiError } from '@/shared/utils/apiError';
import { useSurveyPageFlow } from './useSurveyPageFlow';
import { resolvePreviewNextPageId } from '@/shared/utils/conditions';
import { QuestionControl } from './QuestionControl';
import {
    buildAnswerPayload,
    isQuestionAnswered,
    type AnswerErrors as Errors,
    type Answers,
    type AnswerValue,
} from './answerPayload';

export type SurveyRunnerMode = 'preview' | 'respond';

type SurveyRunnerStage = 'welcome' | 'questions' | 'closing';

type Props = { survey: Survey; mode: 'preview' } | { survey: SurveyPublic; mode: 'respond' };

function sortBySerialNumber<T extends { serialNumber: number }>(items: T[]) {
    return [...items].sort((firstItem, secondItem) => firstItem.serialNumber - secondItem.serialNumber);
}

function isEditablePage(page: Page | SurveyPageShort): page is Page {
    return 'questions' in page && Array.isArray(page.questions) && 'conditions' in page;
}

export function SurveyRunner(props: Props) {
    const { survey, mode } = props;
    const previewSurvey = props.mode === 'preview' ? props.survey : null;
    const [answers, setAnswers] = useState<Answers>({});
    const [errors, setErrors] = useState<Errors>({});
    const [stage, setStage] = useState<SurveyRunnerStage>('welcome');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isStarting, setIsStarting] = useState(false);
    const [otherTexts, setOtherTexts] = useState<Record<string, string>>({});
    const [submitError, setSubmitError] = useState<string | null>(null);
    const isPreview = mode === 'preview';
    const hasCustomClosingPage = mode === 'respond' && survey.hasCustomClosingPage;
    const [closingPage, setClosingPage] = useState<ClosingPage | null>(isPreview ? survey.closingPage : null);
    const [isClosingPageLoading, setIsClosingPageLoading] = useState(false);
    const {
        ensureResponse,
        failedQuestionCount,
        flushPendingAnswers,
        flushQuestion,
        isSaving,
        retryFailedAnswers,
        scheduleAnswerSave,
    } = useSurveyResponseSync(survey.id, isPreview);
    const orderedPages = sortBySerialNumber(survey.pages).map((page) =>
        isEditablePage(page) ? { ...page, questions: sortBySerialNumber(page.questions) } : page,
    );
    const pageFlow = useSurveyPageFlow(survey.id, mode, orderedPages, ensureResponse);

    useEffect(() => {
        setAnswers({});
        setOtherTexts({});
        setErrors({});
        setStage('welcome');
        setSubmitError(null);
        setClosingPage(mode === 'preview' ? survey.closingPage : null);
        setIsClosingPageLoading(false);
    }, [mode, survey]);

    const refreshClosingPage = async (responseId: string) => {
        setClosingPage(null);
        setIsClosingPageLoading(hasCustomClosingPage);
        if (!hasCustomClosingPage) return;

        try {
            setClosingPage(await getClosingPage(survey.id, responseId));
        } catch {
            // ClosingPageView displays the standard completion message as a fallback.
        } finally {
            setIsClosingPageLoading(false);
        }
    };

    const showClosingPagePreview = () => {
        setStage('closing');
    };

    const startSurvey = async () => {
        if (orderedPages.length === 0) {
            setSubmitError('Опрос пока не содержит страниц');
            return;
        }

        setIsStarting(true);
        setSubmitError(null);
        try {
            const firstPage = await pageFlow.start();
            if (!firstPage) {
                setSubmitError('Опрос пока не содержит страниц');
                return;
            }
            setStage('questions');
        } catch (requestError) {
            const apiError = getApiError(requestError);
            setSubmitError(
                apiError?.internalErrorCode === 'SURVEY_IS_EMPTY'
                    ? 'Опрос пока не содержит страниц'
                    : apiError?.message || 'Не удалось начать прохождение опроса',
            );
        } finally {
            setIsStarting(false);
        }
    };

    const updateAnswer = (question: Question, value: AnswerValue) => {
        setAnswers((currentAnswers) => ({
            ...currentAnswers,
            [question.id]: value,
        }));
        setSubmitError(null);
        setErrors((currentErrors) => {
            const nextErrors = { ...currentErrors };
            delete nextErrors[question.id];
            return nextErrors;
        });
        scheduleAnswerSave(question.id, buildAnswerPayload(question, value, otherTexts[question.id] ?? ''), {
            debounce: question.type === 'SHORT_TEXT' || question.type === 'LONG_TEXT',
        });
    };

    const validateQuestions = (questions: Question[]) => {
        const nextErrors: Errors = {};

        questions.forEach((question) => {
            if (question.isMandatory && !isQuestionAnswered(question, answers[question.id], otherTexts[question.id])) {
                nextErrors[question.id] = 'Ответьте на обязательный вопрос';
            }
        });

        setErrors((currentErrors) => {
            const questionIds = new Set(questions.map((question) => question.id));
            const errorsOutsidePage = Object.fromEntries(
                Object.entries(currentErrors).filter(([questionId]) => !questionIds.has(questionId)),
            );

            return {
                ...errorsOutsidePage,
                ...nextErrors,
            };
        });

        return Object.keys(nextErrors).length === 0;
    };

    const currentPageIndex = pageFlow.currentPageIndex;
    const currentPage = pageFlow.currentPage;
    const totalPageCount = orderedPages.length;
    const isFirstPage = pageFlow.isFirstPage;
    const isLastPage = currentPageIndex === totalPageCount - 1;
    const isPageLoading = pageFlow.isPageLoading;

    const goToPreviousPage = async () => {
        if (isPreview) {
            pageFlow.openPrevious();
            return;
        }

        await flushPendingAnswers().catch(() => setSubmitError('Не удалось сохранить некоторые ответы'));
        pageFlow.openPrevious();
    };

    const goToNextPage = async () => {
        if (!currentPage) {
            return;
        }

        if (isPreview) {
            const currentPreviewPage = currentPage;
            if (
                !currentPreviewPage ||
                !isEditablePage(currentPreviewPage) ||
                !validateQuestions(currentPreviewPage.questions)
            )
                return;
            setErrors({});
            const nextPageId = resolvePreviewNextPageId(currentPreviewPage, previewSurvey?.pages ?? [], answers);
            if (!nextPageId) {
                showClosingPagePreview();
                return;
            }
            if (!orderedPages.some(({ id }) => id === nextPageId)) {
                setSubmitError('Условие ведёт на недоступную страницу');
                return;
            }
            await pageFlow.openNext(nextPageId);
            return;
        }

        if (!validateQuestions(currentPage.questions)) {
            return;
        }

        setSubmitError(null);
        setIsSubmitting(true);
        try {
            await flushPendingAnswers();
            const hasNextPage = await pageFlow.openNext();
            if (hasNextPage) return;

            const responseId = await ensureResponse();
            await completeSurveyResponse(responseId);
            setStage('closing');
            await refreshClosingPage(responseId);
        } catch (requestError) {
            const apiError = getApiError(requestError);
            setSubmitError(
                apiError?.internalErrorCode === 'NOT_ALL_MANDATORY_QUESTIONS_ANSWERED'
                    ? 'Ответьте на все обязательные вопросы текущей страницы'
                    : apiError?.message || 'Не удалось определить следующую страницу',
            );
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <main className={style.container}>
            <div className={style.content}>
                {isPreview && (
                    <div className={style.previewActions}>
                        <Button mode='secondary' style='accent' Element={Link} to={routes.surveyQuestions(survey.id)}>
                            Выйти из предпросмотра
                        </Button>
                        <div className={style.previewBadge}>Предпросмотр</div>
                    </div>
                )}
                {stage === 'welcome' ? (
                    <WelcomePageView
                        survey={survey}
                        onStart={() => void startSurvey()}
                        isStarting={isStarting}
                        startError={submitError}
                    />
                ) : stage === 'closing' && isClosingPageLoading ? (
                    <div>Загрузка завершающей страницы...</div>
                ) : stage === 'closing' ? (
                    <ClosingPageView
                        surveyId={survey.id}
                        closingPage={closingPage}
                        onBack={isPreview ? () => setStage('questions') : undefined}
                    />
                ) : (
                    <>
                        {totalPageCount === 0 ? (
                            <div className={surveyDetailStyle.container}>
                                <Text typography='paragraph-2-regular' style='primary'>
                                    В этом опросе пока нет вопросов.
                                </Text>
                                {isPreview && (
                                    <Button mode='primary' style='accent' onClick={showClosingPagePreview}>
                                        Завершающая страница
                                    </Button>
                                )}
                            </div>
                        ) : isPageLoading ? (
                            <div>Загрузка страницы...</div>
                        ) : currentPage ? (
                            <section className={choiceStyle.container}>
                                {(currentPage.description || currentPage.title) && (
                                    <section className={`${surveyDetailStyle.container} ${style.formHeader}`}>
                                        {currentPage.title && (
                                            <HTMLRender className={style.title} html={currentPage.title} />
                                        )}
                                        {currentPage.description && (
                                            <HTMLRender className={style.title} html={currentPage.description} />
                                        )}
                                    </section>
                                )}

                                {currentPage.questions.map((question) => (
                                    <article className={questionStyle.container} key={question.id}>
                                        {question.attachmentUrl && (
                                            <img
                                                src={question.attachmentUrl}
                                                alt='img'
                                                className={style.attachmentUrl}
                                            />
                                        )}
                                        <div className={style.questionTitle}>
                                            <HTMLRender className={style.title} html={question.text} />
                                            {question.isMandatory && <span className={style.mandatory}>*</span>}
                                        </div>
                                        {question.description && (
                                            <div className={style.questionDescription}>
                                                <HTMLRender className={style.description} html={question.description} />
                                            </div>
                                        )}
                                        <section className={questionStyle.actions}>
                                            <div>
                                                <QuestionControl
                                                    question={question}
                                                    value={answers[question.id]}
                                                    otherText={otherTexts[question.id] ?? ''}
                                                    disabled={isSubmitting}
                                                    onChange={(value) => updateAnswer(question, value)}
                                                    onOtherTextChange={(text) => {
                                                        setOtherTexts((current) => ({
                                                            ...current,
                                                            [question.id]: text,
                                                        }));
                                                        scheduleAnswerSave(
                                                            question.id,
                                                            buildAnswerPayload(question, answers[question.id], text),
                                                            { debounce: true },
                                                        );
                                                    }}
                                                    onBlur={() =>
                                                        void flushQuestion(question.id).catch(() => undefined)
                                                    }
                                                />
                                            </div>
                                            <div className={questionStyle.hidden} />
                                        </section>
                                        {errors[question.id] && (
                                            <div className={style.error}>
                                                <Text typography='paragraph-2-regular' style='negative'>
                                                    {errors[question.id]}
                                                </Text>
                                            </div>
                                        )}
                                    </article>
                                ))}

                                <div className={style.navigation}>
                                    <Button
                                        type='button'
                                        mode='secondary'
                                        style='accent'
                                        disabled={isFirstPage || isSubmitting || isPageLoading}
                                        onClick={() => void goToPreviousPage()}
                                    >
                                        Назад
                                    </Button>
                                    <Text typography='paragraph-2-regular' style='secondary'>
                                        {currentPageIndex + 1} из {totalPageCount}
                                    </Text>
                                    <Button
                                        type='button'
                                        mode='primary'
                                        style='accent'
                                        disabled={isSubmitting || isPageLoading}
                                        onClick={() => void goToNextPage()}
                                    >
                                        {isPreview && isLastPage ? 'Завершающая страница' : 'Далее'}
                                    </Button>
                                </div>
                                {failedQuestionCount > 0 && (
                                    <div className={style.syncError}>
                                        <Text typography='paragraph-2-regular' style='negative'>
                                            Не удалось сохранить некоторые ответы
                                        </Text>
                                        <Button
                                            type='button'
                                            mode='secondary'
                                            style='accent'
                                            disabled={isSaving || isSubmitting}
                                            onClick={() => void retryFailedAnswers().catch(() => undefined)}
                                        >
                                            Повторить
                                        </Button>
                                    </div>
                                )}
                                {submitError && (
                                    <div className={style.submitError}>
                                        <Text typography='paragraph-2-regular' style='negative'>
                                            {submitError}
                                        </Text>
                                    </div>
                                )}
                            </section>
                        ) : null}
                    </>
                )}
            </div>
        </main>
    );
}
