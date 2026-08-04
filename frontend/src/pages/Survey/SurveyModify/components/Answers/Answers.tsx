import { Button, createStaticDataProvider, Select, type StaticDataFetcherItem } from '@hh.ru/magritte-ui';
import { Fragment, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';

import { getSurveyById } from '@/api/survey';
import {
    type ResponseAccountDetail,
    type SurveyAnswerResponse,
    type SurveyCompletedResponse,
} from '@/shared/types/SurveyResponse.type';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { downloadBlob } from '@/shared/utils/download';
import { htmlToText } from '@/shared/utils/html';
import { useAppSelector } from '@/hooks/useAppSelector';
import type { Question } from '@/shared/types/Question.type';
import type { Survey } from '@/shared/types/Survey.type';

import style from './Answers.module.css';
import { exportSurveyResponses, getSurveyResponses } from '@/api/surveyResponses';
import { setErrorMessage } from '@/entities/Error/Error.slice';

type AnswersSection = 'summary' | 'question' | 'user';

type QuestionWithPage = Question & {
    pageSerialNumber: number;
};

const SECTION_OPTIONS: Array<{ value: AnswersSection; label: string }> = [
    { value: 'summary', label: 'Сводка' },
    { value: 'question', label: 'Вопрос' },
    { value: 'user', label: 'Отдельный пользователь' },
];

function getSortedQuestions(survey: Survey): QuestionWithPage[] {
    return survey.pages
        .flatMap((page) =>
            page.questions.map((question) => ({
                ...question,
                pageSerialNumber: page.serialNumber,
            })),
        )
        .sort((firstQuestion, secondQuestion) => {
            if (firstQuestion.pageSerialNumber !== secondQuestion.pageSerialNumber) {
                return firstQuestion.pageSerialNumber - secondQuestion.pageSerialNumber;
            }

            return firstQuestion.serialNumber - secondQuestion.serialNumber;
        });
}

function getAnswerByQuestion(response: SurveyCompletedResponse, questionId: string): SurveyAnswerResponse | undefined {
    return response.answers.find((answer) => answer.questionId === questionId);
}

function getQuestionAnswers(responses: SurveyCompletedResponse[], questionId: string) {
    return responses
        .map((response, responseIndex) => ({
            answer: getAnswerByQuestion(response, questionId),
            responseIndex,
        }))
        .filter((item): item is { answer: SurveyAnswerResponse; responseIndex: number } => Boolean(item.answer));
}

function getRespondentLabel(account: ResponseAccountDetail | null) {
    if (account == null) return 'Анонимный пользователь';
    return `${account.login}: ${account.email}`;
}

function getSelectValue(options: StaticDataFetcherItem[], value: string) {
    return options.find((option) => option.value === value);
}

export function Answers() {
    const { id } = useParams();
    const dispatch = useAppDispatch();
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const [survey, setSurvey] = useState<Survey | null>(selectedSurvey);
    const [responses, setResponses] = useState<SurveyCompletedResponse[]>([]);
    const [activeSection, setActiveSection] = useState<AnswersSection>('summary');
    const [selectedQuestionIndex, setSelectedQuestionIndex] = useState(0);
    const [selectedResponseIndex, setSelectedResponseIndex] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [isExporting, setIsExporting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) {
            setError('Опрос не найден');
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
        setError(null);

        Promise.all([
            selectedSurvey?.id === id ? Promise.resolve(selectedSurvey) : getSurveyById(id),
            getSurveyResponses(id),
        ])
            .then(([surveyData, responsesData]) => {
                setSurvey(surveyData);
                setResponses(responsesData);
                dispatch(setSelectedSurvey({ survey: surveyData }));
            })
            .catch(() => {
                setError('Не удалось загрузить ответы');
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, [dispatch, id, selectedSurvey]);

    const questions = useMemo(() => (survey ? getSortedQuestions(survey) : []), [survey]);

    const questionOptions = useMemo<StaticDataFetcherItem[]>(
        () =>
            questions.map((question, index) => ({
                value: String(index),
                text: htmlToText(question.text),
            })),
        [questions],
    );

    const responseOptions = useMemo<StaticDataFetcherItem[]>(
        () =>
            responses.map((response, index) => ({
                value: String(index),
                text: getRespondentLabel(response.account),
            })),
        [responses],
    );

    const currentQuestion = questions[selectedQuestionIndex];
    const currentResponse = responses[selectedResponseIndex];

    const switchQuestion = (direction: -1 | 1) => {
        if (questions.length === 0) return;
        setSelectedQuestionIndex((currentIndex) => {
            return (currentIndex + direction + questions.length) % questions.length;
        });
    };

    const switchResponse = (direction: -1 | 1) => {
        if (responses.length === 0) return;
        setSelectedResponseIndex((currentIndex) => {
            return (currentIndex + direction + responses.length) % responses.length;
        });
    };

    const openResponse = (responseIndex: number) => {
        setSelectedResponseIndex(responseIndex);
        setActiveSection('user');
    };

    const handleExport = async () => {
        if (!id || isExporting) return;

        setIsExporting(true);

        try {
            const { file, filename } = await exportSurveyResponses(id);
            downloadBlob(file, filename);
        } catch {
            dispatch(setErrorMessage({ message: 'Не удалось скачать ответы' }));
        } finally {
            setIsExporting(false);
        }
    };

    if (isLoading) {
        return <div className={style.status}>Загрузка...</div>;
    }

    if (error || !survey) {
        return <div className={style.status}>{error ?? 'Опрос не найден'}</div>;
    }

    return (
        <section className={style.container}>
            <div className={style.content}>
                <div className={style.sections}>
                    {SECTION_OPTIONS.map((section) => (
                        <Button
                            key={section.value}
                            mode={activeSection === section.value ? 'primary' : 'secondary'}
                            style='accent'
                            onClick={() => setActiveSection(section.value)}
                        >
                            {section.label}
                        </Button>
                    ))}
                </div>

                <div className={style.respondentsRow}>
                    <div className={style.respondents}>Количество опрошенных пользователей: {responses.length}</div>
                    <Button
                        mode='secondary'
                        style='positive'
                        loading={isExporting}
                        disabled={isExporting}
                        onClick={handleExport}
                    >
                        Скачать ответы (xlsx)
                    </Button>
                </div>

                {responses.length === 0 && <div className={style.empty}>Ответов пока нет</div>}

                {responses.length > 0 && activeSection === 'summary' && (
                    <div className={style.list}>
                        {questions.map((question) => {
                            const answers = getQuestionAnswers(responses, question.id);

                            return (
                                <article className={style.questionBlock} key={question.id}>
                                    <h2 className={style.questionTitle}>{htmlToText(question.text)}</h2>
                                    {answers.length === 0 ? (
                                        <div className={style.empty}>Нет ответов на этот вопрос</div>
                                    ) : (
                                        <ul className={style.answers} key={question.id}>
                                            {answers.map(({ answer, responseIndex }) => (
                                                <Fragment key={answer.responseId}>
                                                    {answer.selectedAnswerOptions.map((answerText) => (
                                                        <li className={style.answer} key={answerText.id}>
                                                            <button
                                                                className={style.answerButton}
                                                                type='button'
                                                                onClick={() => openResponse(responseIndex)}
                                                            >
                                                                {htmlToText(answerText.answerOptionTextSnapshot)}
                                                            </button>
                                                        </li>
                                                    ))}
                                                    {answer.textValue && (
                                                        <li
                                                            className={style.answer}
                                                            key={`${answer.responseId}-${answer.questionId}`}
                                                        >
                                                            <button
                                                                className={style.answerButton}
                                                                type='button'
                                                                onClick={() => openResponse(responseIndex)}
                                                            >
                                                                {htmlToText(answer.textValue)}
                                                            </button>
                                                        </li>
                                                    )}
                                                </Fragment>
                                            ))}
                                        </ul>
                                    )}
                                </article>
                            );
                        })}
                    </div>
                )}

                {responses.length > 0 && activeSection === 'question' && currentQuestion && (
                    <div className={style.list}>
                        <div className={style.switcher}>
                            <Button
                                className={style.switcherArrow}
                                mode='secondary'
                                style='accent'
                                onClick={() => switchQuestion(-1)}
                            >
                                ←
                            </Button>
                            <div className={style.switcherSelect}>
                                <Select
                                    type='label'
                                    value={getSelectValue(questionOptions, String(selectedQuestionIndex))}
                                    dataProvider={createStaticDataProvider(questionOptions, 'Вопрос')}
                                    name='question'
                                    widthEqualToActivator={false}
                                    dropWidth={300}
                                    onChange={(option) => setSelectedQuestionIndex(Number(option.value))}
                                />
                            </div>
                            <Button
                                className={style.switcherArrow}
                                mode='secondary'
                                style='accent'
                                onClick={() => switchQuestion(1)}
                            >
                                →
                            </Button>
                        </div>

                        <article className={style.questionBlock}>
                            <h2 className={style.questionTitle}>{htmlToText(currentQuestion.text)}</h2>
                            {getQuestionAnswers(responses, currentQuestion.id).length === 0 ? (
                                <div className={style.empty}>Нет ответов на этот вопрос</div>
                            ) : (
                                <ul className={style.answers} key={'question_id'}>
                                    {getQuestionAnswers(responses, currentQuestion.id).map(
                                        ({ answer, responseIndex }) => (
                                            <Fragment key={answer.responseId}>
                                                {answer.selectedAnswerOptions.map((answerText) => (
                                                    <li className={style.answer} key={answerText.id}>
                                                        <button
                                                            className={style.answerButton}
                                                            type='button'
                                                            onClick={() => openResponse(responseIndex)}
                                                        >
                                                            {htmlToText(answerText.answerOptionTextSnapshot)}
                                                        </button>
                                                    </li>
                                                ))}
                                                {answer.textValue && (
                                                    <li
                                                        className={style.answer}
                                                        key={`${answer.responseId}-${answer.questionId}`}
                                                    >
                                                        <button
                                                            className={style.answerButton}
                                                            type='button'
                                                            onClick={() => openResponse(responseIndex)}
                                                        >
                                                            {htmlToText(answer.textValue)}
                                                        </button>
                                                    </li>
                                                )}
                                            </Fragment>
                                        ),
                                    )}
                                </ul>
                            )}
                        </article>
                    </div>
                )}

                {responses.length > 0 && activeSection === 'user' && currentResponse && (
                    <div className={style.list}>
                        <div className={style.switcher}>
                            <Button
                                className={style.switcherArrow}
                                mode='secondary'
                                style='accent'
                                onClick={() => switchResponse(-1)}
                            >
                                ←
                            </Button>
                            <div className={style.switcherSelect}>
                                <Select
                                    type='label'
                                    value={getSelectValue(responseOptions, String(selectedResponseIndex))}
                                    dataProvider={createStaticDataProvider(responseOptions, 'Пользователь')}
                                    name='response'
                                    widthEqualToActivator={false}
                                    dropWidth={300}
                                    onChange={(option) => setSelectedResponseIndex(Number(option.value))}
                                />
                            </div>
                            <Button
                                className={style.switcherArrow}
                                mode='secondary'
                                style='accent'
                                onClick={() => switchResponse(1)}
                            >
                                →
                            </Button>
                        </div>

                        <div className={style.list}>
                            {questions.map((question) => {
                                const answer = getAnswerByQuestion(currentResponse, question.id);
                                return (
                                    <article className={style.questionBlock} key={question.id}>
                                        <h2 className={style.questionTitle}>{htmlToText(question.text)}</h2>
                                        {answer &&
                                            (answer.selectedAnswerOptions.length !== 0
                                                ? answer.selectedAnswerOptions.map((answerValue) => (
                                                      <div className={style.singleAnswer} key={answerValue.id}>
                                                          {htmlToText(answerValue.answerOptionTextSnapshot)}
                                                      </div>
                                                  ))
                                                : answer.textValue && (
                                                      <div
                                                          className={style.singleAnswer}
                                                          key={`${answer.responseId}-${answer.questionId}`}
                                                      >
                                                          {htmlToText(answer.textValue)}
                                                      </div>
                                                  ))}
                                    </article>
                                );
                            })}
                        </div>
                    </div>
                )}
            </div>
        </section>
    );
}
