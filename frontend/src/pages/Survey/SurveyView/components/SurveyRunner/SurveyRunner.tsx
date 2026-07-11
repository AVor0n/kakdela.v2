import { useEffect, useState } from 'react';
import type { Question } from '@/shared/types/Question.type';
import type { Survey } from '@/shared/types/Survey.type';
import { Button, Checkbox, Input, Radio, Tag, Text, TextArea, TextAreaGrowLimiter, Title } from '@hh.ru/magritte-ui';
import { Link } from 'react-router-dom';
import { routes } from '@/app/routes';
import { completeSurveyResponse, createSurveyAnswer, createSurveyResponse } from '@/api/surveyResponses';
import surveyDetailStyle from '@/pages/Survey/SurveyModify/components/SurveyDetail/SurveyDetail.module.css';
import pageSeparatorStyle from '@/pages/Survey/SurveyModify/components/PageSeparator/PageSeparator.module.css';
import questionStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/Question.module.css';
import choiceStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/Choice/Choice.module.css';
import optionStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/Choice/components/Option/Option.module.css';
import longTextStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/LongText/LongText.module.css';
import style from './SurveyRunner.module.css';
import { useAppSelector } from '@/hooks/useAppSelector';
import { AccountDetail } from '@/shared/ui/AccountDetail/AccountDetail';

export type SurveyRunnerMode = 'preview' | 'respond';

type AnswerValue = string | string[];
type Answers = Record<string, AnswerValue>;
type Errors = Record<string, string>;

type Props = {
    survey: Survey;
    mode: SurveyRunnerMode;
};

function sortBySerialNumber<T extends { serialNumber: number }>(items: T[]) {
    return [...items].sort((firstItem, secondItem) => firstItem.serialNumber - secondItem.serialNumber);
}

function isQuestionAnswered(question: Question, value: AnswerValue | undefined) {
    if (question.type === 'MULTIPLE_CHOICE') {
        return Array.isArray(value) && value.length > 0;
    }

    return typeof value === 'string' && value.trim().length > 0;
}

function isQuestionVisible(question: Question) {
    return question.visible ?? question.isVisible ?? true;
}

function getAnswerText(question: Question, value: AnswerValue | undefined) {
    if (value === undefined) {
        return '';
    }

    if (question.type === 'SINGLE_CHOICE') {
        return question.answerOptions.find((option) => option.id === value)?.answerOptionText ?? '';
    }

    if (question.type === 'MULTIPLE_CHOICE') {
        const selectedOptionIds = Array.isArray(value) ? value : [];

        return question.answerOptions
            .filter((option) => selectedOptionIds.includes(option.id))
            .map((option) => option.answerOptionText)
            .join(', ');
    }

    return typeof value === 'string' ? value.trim() : '';
}

export function SurveyRunner({ survey, mode }: Props) {
    const [currentPageIndex, setCurrentPageIndex] = useState(0);
    const [answers, setAnswers] = useState<Answers>({});
    const [errors, setErrors] = useState<Errors>({});
    const [isComplete, setIsComplete] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);
    const isPreview = mode === 'preview';
    const { account } = useAppSelector((state) => state.account);

    useEffect(() => {
        setCurrentPageIndex(0);
        setAnswers({});
        setErrors({});
        setIsComplete(false);
        setSubmitError(null);
    }, [survey.id]);

    const updateAnswer = (questionId: string, value: AnswerValue) => {
        setAnswers((currentAnswers) => ({
            ...currentAnswers,
            [questionId]: value,
        }));
        setSubmitError(null);
        setErrors((currentErrors) => {
            const nextErrors = { ...currentErrors };
            delete nextErrors[questionId];
            return nextErrors;
        });
    };

    const toggleMultipleChoice = (questionId: string, optionId: string) => {
        const currentValue = answers[questionId];
        const selectedOptions = Array.isArray(currentValue) ? currentValue : [];
        const nextValue = selectedOptions.includes(optionId)
            ? selectedOptions.filter((selectedOptionId) => selectedOptionId !== optionId)
            : [...selectedOptions, optionId];

        updateAnswer(questionId, nextValue);
    };

    const validate = () => {
        const nextErrors: Errors = {};

        survey.pages.forEach((page) => {
            page.questions.filter(isQuestionVisible).forEach((question) => {
                if (question.isMandatory && !isQuestionAnswered(question, answers[question.id])) {
                    nextErrors[question.id] = 'Ответьте на обязательный вопрос';
                }
            });
        });

        setErrors(nextErrors);
        return Object.keys(nextErrors).length === 0;
    };

    const validateQuestions = (questions: Question[]) => {
        const nextErrors: Errors = {};

        questions.forEach((question) => {
            if (question.isMandatory && !isQuestionAnswered(question, answers[question.id])) {
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

    const prepareAnswers = () => {
        return survey.pages.flatMap((page) =>
            page.questions
                .filter(isQuestionVisible)
                .map((question) => ({
                    questionId: question.id,
                    answerText: getAnswerText(question, answers[question.id]),
                }))
                .filter((answer) => answer.answerText.trim().length > 0),
        );
    };

    const submitHandler = async () => {
        if (isPreview || !validate()) {
            return;
        }

        setIsSubmitting(true);
        setSubmitError(null);

        try {
            const response = await createSurveyResponse(survey.id);

            for (const answer of prepareAnswers()) {
                await createSurveyAnswer(response.id, answer.questionId, answer.answerText);
            }

            await completeSurveyResponse(response.id);
            setIsComplete(true);
        } catch {
            setSubmitError('Не удалось отправить ответы');
        } finally {
            setIsSubmitting(false);
        }
    };

    const renderQuestionControl = (question: Question) => {
        const value = answers[question.id];

        switch (question.type) {
            case 'SHORT_TEXT':
                return (
                    <Input
                        placeholder='Короткий текст'
                        size='large'
                        value={typeof value === 'string' ? value : ''}
                        onChange={(nextValue) => updateAnswer(question.id, nextValue)}
                    />
                );
            case 'LONG_TEXT':
                return (
                    <TextAreaGrowLimiter className={longTextStyle.content}>
                        <TextArea
                            placeholder='Длинный текст'
                            value={typeof value === 'string' ? value : ''}
                            onChange={(event) => updateAnswer(question.id, event.target.value)}
                            size='large'
                            layout='hug'
                        />
                    </TextAreaGrowLimiter>
                );
            case 'SINGLE_CHOICE':
                return (
                    <div className={choiceStyle.container}>
                        {sortBySerialNumber(question.answerOptions).map((option) => (
                            <div className={optionStyle.optionContent} key={option.id}>
                                <label className={optionStyle.option}>
                                    <Radio
                                        name={question.id}
                                        checked={value === option.id}
                                        onChange={() => updateAnswer(question.id, option.id)}
                                    />
                                    <Text typography='paragraph-2-regular' style='primary'>
                                        {option.answerOptionText}
                                    </Text>
                                </label>
                            </div>
                        ))}
                    </div>
                );
            case 'MULTIPLE_CHOICE':
                return (
                    <div className={choiceStyle.container}>
                        {sortBySerialNumber(question.answerOptions).map((option) => {
                            const selectedOptions = Array.isArray(value) ? value : [];

                            return (
                                <div className={optionStyle.optionContent} key={option.id}>
                                    <label className={optionStyle.option}>
                                        <Checkbox
                                            checked={selectedOptions.includes(option.id)}
                                            onChange={() => toggleMultipleChoice(question.id, option.id)}
                                        />
                                        <Text typography='paragraph-2-regular' style='primary'>
                                            {option.answerOptionText}
                                        </Text>
                                    </label>
                                </div>
                            );
                        })}
                    </div>
                );
            default:
                return null;
        }
    };

    const visiblePages = sortBySerialNumber(survey.pages)
        .map((page) => ({
            ...page,
            questions: sortBySerialNumber(page.questions).filter(isQuestionVisible),
        }))
        .filter((page) => page.questions.length > 0);
    const currentPage = visiblePages[currentPageIndex];
    const isFirstPage = currentPageIndex === 0;
    const isLastPage = currentPageIndex === visiblePages.length - 1;

    const goToPreviousPage = () => {
        setCurrentPageIndex((pageIndex) => Math.max(pageIndex - 1, 0));
    };

    const goToNextPage = () => {
        if (!currentPage) {
            return;
        }

        if (isPreview) {
            setErrors({});
            setCurrentPageIndex((pageIndex) => Math.min(pageIndex + 1, visiblePages.length - 1));
            return;
        }

        if (!validateQuestions(currentPage.questions)) {
            return;
        }

        setCurrentPageIndex((pageIndex) => Math.min(pageIndex + 1, visiblePages.length - 1));
    };

    return (
        <>
            <div className={style.accountDetail}>
                {account !== null ? <AccountDetail /> : <Tag>Анномное прохождение</Tag>}
            </div>
            <main className={style.container}>
                <div className={style.content}>
                    {isPreview && (
                        <div className={style.previewActions}>
                            <Button
                                mode='secondary'
                                style='accent'
                                Element={Link}
                                to={routes.surveyQuestions(survey.id)}
                            >
                                Выйти из предпросмотра
                            </Button>
                            <div className={style.previewBadge}>Предпросмотр</div>
                        </div>
                    )}
                    <header className={`${surveyDetailStyle.container} ${style.formHeader}`}>
                        <Text typography='subtitle-1-semibold' style='primary'>
                            {survey.title}
                        </Text>
                        {survey.description && (
                            <Text typography='paragraph-2-regular' style='primary'>
                                {survey.description}
                            </Text>
                        )}
                    </header>

                    {isComplete ? (
                        <section className={surveyDetailStyle.container}>
                            <Title Element='h2' size='medium'>
                                Спасибо за ответ
                            </Title>
                            <Text typography='paragraph-2-regular' style='primary'>
                                {survey.closingPage ?? 'Ваши ответы приняты.'}
                            </Text>
                        </section>
                    ) : (
                        <>
                            {visiblePages.length === 0 ? (
                                <div className={surveyDetailStyle.container}>В этом опросе пока нет вопросов.</div>
                            ) : currentPage ? (
                                <section className={choiceStyle.container}>
                                    <div className={pageSeparatorStyle.separator}>
                                        <span>Страница {currentPage.serialNumber}</span>
                                    </div>

                                    {currentPage.questions.map((question) => (
                                        <article className={questionStyle.container} key={question.id}>
                                            <div className={style.questionTitle}>
                                                <Text typography='paragraph-2-regular' style='primary'>
                                                    {question.title}
                                                </Text>
                                                {question.isMandatory && <span className={style.mandatory}>*</span>}
                                            </div>
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
                                            disabled={isFirstPage}
                                            onClick={goToPreviousPage}
                                        >
                                            Назад
                                        </Button>
                                        <Text typography='paragraph-2-regular' style='secondary'>
                                            {currentPageIndex + 1} из {visiblePages.length}
                                        </Text>
                                        {isLastPage ? (
                                            <Button
                                                type='button'
                                                mode='primary'
                                                style='accent'
                                                disabled={isPreview || isSubmitting}
                                                onClick={submitHandler}
                                            >
                                                {isPreview
                                                    ? 'Отправка недоступна в предпросмотре'
                                                    : isSubmitting
                                                      ? 'Отправляем...'
                                                      : 'Отправить'}
                                            </Button>
                                        ) : (
                                            <Button type='button' mode='primary' style='accent' onClick={goToNextPage}>
                                                Далее
                                            </Button>
                                        )}
                                    </div>
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
        </>
    );
}
