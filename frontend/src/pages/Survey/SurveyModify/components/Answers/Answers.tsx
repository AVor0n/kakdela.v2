import { Button, createStaticDataProvider, Select, type StaticDataFetcherItem } from '@hh.ru/magritte-ui';
import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';

import { getSurveyById } from '@/api/survey';
import { getSurveyResponses, type SurveyAnswerResponse, type SurveyCompletedResponse } from '@/api/surveyResponses';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { useAppSelector } from '@/hooks/useAppSelector';
import type { Question } from '@/shared/types/Question.type';
import type { Survey } from '@/shared/types/Survey.type';

import style from './Answers.module.css';

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

function getRespondentLabel(index: number) {
    return `Пользователь ${index + 1}`;
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
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) {
            setError('Опрос не найден');
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
        setError(null);

        Promise.all([selectedSurvey?.id === id ? Promise.resolve(selectedSurvey) : getSurveyById(id), getSurveyResponses(id)])
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
                text: question.title,
            })),
        [questions],
    );

    const responseOptions = useMemo<StaticDataFetcherItem[]>(
        () =>
            responses.map((_, index) => ({
                value: String(index),
                text: getRespondentLabel(index),
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

                <div className={style.respondents}>Количество опрошенных пользователей: {responses.length}</div>

                {responses.length === 0 && <div className={style.empty}>Ответов пока нет</div>}

                {responses.length > 0 && activeSection === 'summary' && (
                    <div className={style.list}>
                        {questions.map((question) => {
                            const answers = getQuestionAnswers(responses, question.id);

                            return (
                                <article className={style.questionBlock} key={question.id}>
                                    <h2 className={style.questionTitle}>{question.title}</h2>
                                    {answers.length === 0 ? (
                                        <div className={style.empty}>Нет ответов на этот вопрос</div>
                                    ) : (
                                        <ul className={style.answers}>
                                            {answers.map(({ answer, responseIndex }) => (
                                                <li className={style.answer} key={`${answer.responseId}-${answer.questionId}`}>
                                                    <button
                                                        className={style.answerButton}
                                                        type='button'
                                                        onClick={() => openResponse(responseIndex)}
                                                    >
                                                        {answer.answerText}
                                                    </button>
                                                </li>
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
                            <h2 className={style.questionTitle}>{currentQuestion.title}</h2>
                            {getQuestionAnswers(responses, currentQuestion.id).length === 0 ? (
                                <div className={style.empty}>Нет ответов на этот вопрос</div>
                            ) : (
                                <ul className={style.answers}>
                                    {getQuestionAnswers(responses, currentQuestion.id).map(({ answer, responseIndex }) => (
                                        <li className={style.answer} key={`${answer.responseId}-${answer.questionId}`}>
                                            <button
                                                className={style.answerButton}
                                                type='button'
                                                onClick={() => openResponse(responseIndex)}
                                            >
                                                {answer.answerText}
                                            </button>
                                        </li>
                                    ))}
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
                                        <h2 className={style.questionTitle}>{question.title}</h2>
                                        <div className={style.singleAnswer}>{answer?.answerText ?? 'Нет ответа'}</div>
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
