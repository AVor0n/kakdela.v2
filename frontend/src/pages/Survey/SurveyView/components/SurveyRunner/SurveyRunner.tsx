import { useEffect, useState } from 'react';
import type { Question } from '@/shared/types/Question.type';
import type { ClosingPage, Survey, SurveyPublic } from '@/shared/types/Survey.type';
import { Button, Checkbox, Input, Radio, Text, TextArea, TextAreaGrowLimiter } from '@hh.ru/magritte-ui';
import { Link } from 'react-router-dom';
import { routes } from '@/app/routes';
import { completeSurveyResponse } from '@/api/surveyResponses';
import surveyDetailStyle from '@/pages/Survey/SurveyModify/components/SurveyDetail/SurveyDetail.module.css';
import questionStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/Question.module.css';
import choiceStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/Choice/Choice.module.css';
import optionStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/Choice/components/Option/Option.module.css';
import longTextStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/LongText/LongText.module.css';
import style from './SurveyRunner.module.css';
import { useSurveyResponseSync } from './useSurveyResponseSync';
import { HTMLRender } from '@/shared/ui/HTMLRender/HTMLRender';
import { ClosingPageView } from '../ClosingPageView/ClosingPageView';
import { WelcomePageView } from '../WelcomePageView/WelcomePageView';
import { getClosingPage } from '@/api/closingPage';
import { getApiError } from '@/shared/utils/apiError';
import { useSurveyPageFlow } from './useSurveyPageFlow';
import { resolvePreviewNextPageId } from '@/shared/utils/conditions';

const OTHER_OPTION_VALUE = '__other__';

export type SurveyRunnerMode = 'preview' | 'respond';

type AnswerValue = string | string[] | boolean;
type Answers = Record<string, AnswerValue>;
type Errors = Record<string, string>;
type SurveyRunnerStage = 'welcome' | 'questions' | 'closing';

type Props = { survey: Survey; mode: 'preview' } | { survey: SurveyPublic; mode: 'respond' };

function sortBySerialNumber<T extends { serialNumber: number }>(items: T[]) {
    return [...items].sort((firstItem, secondItem) => firstItem.serialNumber - secondItem.serialNumber);
}

function isQuestionAnswered(question: Question, value: AnswerValue | undefined, otherText: string | undefined) {
    switch (question.type) {
        case 'MULTIPLE_CHOICE': {
            if (!Array.isArray(value) || value.length === 0) return false;
            return !value.includes(OTHER_OPTION_VALUE) || Boolean((otherText ?? '').trim());
        }
        case 'YES_NO':
            return typeof value === 'boolean';
        case 'SHORT_TEXT':
        case 'LONG_TEXT':
        case 'DATE':
        case 'TIME':
            return typeof value === 'string' && value.trim().length > 0;
        case 'SINGLE_CHOICE':
            if (typeof value !== 'string' || !value.trim()) return false;
            return value !== OTHER_OPTION_VALUE || Boolean((otherText ?? '').trim());
    }
}

function buildMultipleChoicePayload(selectedIds: string[], otherText: string) {
    const normalIds = selectedIds.filter((id) => id !== OTHER_OPTION_VALUE);
    const trimmedOtherText = otherText.trim();
    const otherSelected = selectedIds.includes(OTHER_OPTION_VALUE) && trimmedOtherText.length > 0;

    if (!otherSelected) {
        return { selectedAnswerOptionIds: normalIds };
    }
    return { selectedAnswerOptionIds: normalIds, textValue: trimmedOtherText };
}

function buildAnswerPayload(question: Question, value: AnswerValue | undefined, otherText: string) {
    switch (question.type) {
        case 'SINGLE_CHOICE':
            if (value === OTHER_OPTION_VALUE) return { textValue: otherText.trim() };
            return { selectedAnswerOptionIds: typeof value === 'string' && value ? [value] : [] };
        case 'MULTIPLE_CHOICE':
            return buildMultipleChoicePayload(Array.isArray(value) ? value : [], otherText);
        case 'YES_NO':
            return { booleanValue: typeof value === 'boolean' ? value : undefined };
        case 'DATE':
            return { dateValue: typeof value === 'string' && value ? value : undefined };
        case 'TIME':
            return { timeValue: typeof value === 'string' && value ? value : undefined };
        case 'SHORT_TEXT':
        case 'LONG_TEXT':
            return { textValue: typeof value === 'string' ? value.trim() : '' };
    }
}

export function SurveyRunner({ survey, mode }: Props) {
    const [previewPageIndex, setPreviewPageIndex] = useState(0);
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
    const orderedPageSummaries = sortBySerialNumber(survey.pages);
    const responseFlow = useSurveyPageFlow(survey.id, mode, orderedPageSummaries, ensureResponse);

    useEffect(() => {
        setPreviewPageIndex(0);
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
        if (orderedPageSummaries.length === 0) {
            setSubmitError('Опрос пока не содержит страниц');
            return;
        }

        if (isPreview) {
            setStage('questions');
            return;
        }

        setIsStarting(true);
        setSubmitError(null);
        try {
            const firstPage = await responseFlow.start();
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

    const toggleMultipleChoice = (question: Question, optionId: string) => {
        const currentValue = answers[question.id];
        const selectedOptions = Array.isArray(currentValue) ? currentValue : [];
        const nextValue = selectedOptions.includes(optionId)
            ? selectedOptions.filter((selectedOptionId) => selectedOptionId !== optionId)
            : [...selectedOptions, optionId];

        updateAnswer(question, nextValue);
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

    const renderQuestionControl = (question: Question) => {
        const value = answers[question.id];

        switch (question.type) {
            case 'SHORT_TEXT':
                return (
                    <Input
                        placeholder='Короткий текст'
                        size='large'
                        disabled={isSubmitting}
                        value={typeof value === 'string' ? value : ''}
                        onChange={(nextValue) => updateAnswer(question, nextValue)}
                        onBlur={() => void flushQuestion(question.id).catch(() => undefined)}
                    />
                );
            case 'LONG_TEXT':
                return (
                    <TextAreaGrowLimiter className={longTextStyle.content}>
                        <TextArea
                            placeholder='Длинный текст'
                            disabled={isSubmitting}
                            value={typeof value === 'string' ? value : ''}
                            onChange={(event) => updateAnswer(question, event.target.value)}
                            onBlur={() => void flushQuestion(question.id).catch(() => undefined)}
                            size='large'
                            layout='hug'
                        />
                    </TextAreaGrowLimiter>
                );
            case 'SINGLE_CHOICE':
                return (
                    <div className={choiceStyle.container}>
                        {question.answerOptions.map((option) => {
                            return (
                                <div className={optionStyle.optionContent} key={option.id}>
                                    <label className={optionStyle.option}>
                                        <Radio
                                            name={question.id}
                                            disabled={isSubmitting}
                                            checked={value === option.id}
                                            onChange={() => updateAnswer(question, option.id)}
                                        />
                                        <Text typography='paragraph-2-regular' style='primary'>
                                            <HTMLRender html={option.text} />
                                        </Text>
                                    </label>
                                </div>
                            );
                        })}
                        {question.hasOtherOption && (
                            <div className={style.anotherOption}>
                                <Radio
                                    name={question.id}
                                    disabled={isSubmitting}
                                    checked={value === OTHER_OPTION_VALUE}
                                    onChange={() => updateAnswer(question, OTHER_OPTION_VALUE)}
                                />
                                <p>Другое: </p>
                                <input
                                    className={style.another}
                                    disabled={value !== OTHER_OPTION_VALUE}
                                    value={otherTexts[question.id] ?? ''}
                                    onChange={(e) => {
                                        const text = e.target.value;
                                        setOtherTexts((prev) => ({ ...prev, [question.id]: text }));
                                        scheduleAnswerSave(question.id, buildAnswerPayload(question, value, text), {
                                            debounce: true,
                                        });
                                    }}
                                />
                            </div>
                        )}
                    </div>
                );
            case 'MULTIPLE_CHOICE':
                return (
                    <div className={choiceStyle.container}>
                        {question.answerOptions.map((option) => {
                            const selectedOptions = Array.isArray(value) ? value : [];
                            return (
                                <div className={optionStyle.optionContent} key={option.id}>
                                    <label className={optionStyle.option}>
                                        <Checkbox
                                            disabled={isSubmitting}
                                            checked={selectedOptions.includes(option.id)}
                                            onChange={() => toggleMultipleChoice(question, option.id)}
                                        />
                                        <Text typography='paragraph-2-regular' style='primary'>
                                            <HTMLRender html={option.text} />
                                        </Text>
                                    </label>
                                </div>
                            );
                        })}
                        {question.hasOtherOption && (
                            <div className={style.anotherOption}>
                                <Checkbox
                                    disabled={isSubmitting}
                                    checked={Array.isArray(value) && value.includes(OTHER_OPTION_VALUE)}
                                    onChange={() => toggleMultipleChoice(question, OTHER_OPTION_VALUE)}
                                />
                                <p>Другое: </p>
                                <input
                                    className={style.another}
                                    disabled={!Array.isArray(value) || !value.includes(OTHER_OPTION_VALUE)}
                                    value={otherTexts[question.id] ?? ''}
                                    onChange={(e) => {
                                        const text = e.target.value;
                                        setOtherTexts((prev) => ({ ...prev, [question.id]: text }));
                                        scheduleAnswerSave(question.id, buildAnswerPayload(question, value, text), {
                                            debounce: true,
                                        });
                                    }}
                                />
                            </div>
                        )}
                    </div>
                );
            case 'YES_NO':
                return (
                    <div className={choiceStyle.container}>
                        <label className={optionStyle.option}>
                            <Radio
                                name={question.id}
                                disabled={isSubmitting}
                                checked={value === true}
                                onChange={() => updateAnswer(question, true)}
                            />
                            <Text typography='paragraph-2-regular' style='primary'>
                                Да
                            </Text>
                        </label>
                        <label className={optionStyle.option}>
                            <Radio
                                name={question.id}
                                disabled={isSubmitting}
                                checked={value === false}
                                onChange={() => updateAnswer(question, false)}
                            />
                            <Text typography='paragraph-2-regular' style='primary'>
                                Нет
                            </Text>
                        </label>
                    </div>
                );
            case 'DATE':
                return (
                    <input
                        className={style.temporalInput}
                        type='date'
                        disabled={isSubmitting}
                        value={typeof value === 'string' ? value : ''}
                        onChange={(event) => updateAnswer(question, event.target.value)}
                    />
                );
            case 'TIME':
                return (
                    <input
                        className={style.temporalInput}
                        type='time'
                        disabled={isSubmitting}
                        value={typeof value === 'string' ? value : ''}
                        onChange={(event) => updateAnswer(question, event.target.value)}
                    />
                );
            default:
                return null;
        }
    };

    const previewPages = isPreview
        ? sortBySerialNumber(survey.pages).map((page) => ({
              ...page,
              questions: sortBySerialNumber(page.questions),
          }))
        : [];
    const currentPageIndex = isPreview ? previewPageIndex : responseFlow.currentPageIndex;
    const currentPage = isPreview ? previewPages[previewPageIndex] : responseFlow.currentPage;
    const totalPageCount = orderedPageSummaries.length;
    const isFirstPage = isPreview ? previewPageIndex === 0 : responseFlow.isFirstPage;
    const isLastPage = currentPageIndex === totalPageCount - 1;
    const isPageLoading = responseFlow.isPageLoading;

    const goToPreviousPage = async () => {
        if (isPreview) {
            setPreviewPageIndex((pageIndex) => Math.max(pageIndex - 1, 0));
            return;
        }

        await flushPendingAnswers().catch(() => setSubmitError('Не удалось сохранить некоторые ответы'));
        responseFlow.openPrevious();
    };

    const goToNextPage = async () => {
        if (!currentPage) {
            return;
        }

        if (isPreview) {
            const currentPreviewPage = previewPages[previewPageIndex];
            if (!currentPreviewPage || !validateQuestions(currentPreviewPage.questions)) return;
            setErrors({});
            const nextPageId = resolvePreviewNextPageId(currentPreviewPage, survey.pages, answers);
            if (!nextPageId) {
                showClosingPagePreview();
                return;
            }

            const nextPageIndex = previewPages.findIndex(({ id }) => id === nextPageId);
            if (nextPageIndex < 0) {
                setSubmitError('Условие ведёт на недоступную страницу');
                return;
            }
            setPreviewPageIndex(nextPageIndex);
            return;
        }

        if (!validateQuestions(currentPage.questions)) {
            return;
        }

        setSubmitError(null);
        setIsSubmitting(true);
        try {
            await flushPendingAnswers();
            const hasNextPage = await responseFlow.verifyAndOpenNext(currentPage.id);
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
                                            <div>{renderQuestionControl(question)}</div>
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
