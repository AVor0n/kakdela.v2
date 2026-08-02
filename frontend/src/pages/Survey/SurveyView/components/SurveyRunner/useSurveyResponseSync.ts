import axios from 'axios';
import { useCallback, useEffect, useRef, useState } from 'react';
import {
    createSurveyResponse,
    deleteSurveyAnswer,
    updateSurveyAnswer,
    type SurveyAnswerRequest,
} from '@/api/surveyResponses';

const TEXT_ANSWER_DEBOUNCE_MS = 600;
const MAX_PARALLEL_REQUESTS = 3;

type ScheduleOptions = {
    debounce?: boolean;
};

export function useSurveyResponseSync(surveyId: string, disabled: boolean) {
    const responseIdRef = useRef<string | null>(null);
    const responsePromiseRef = useRef<Promise<string> | null>(null);
    const persistedQuestionIdsRef = useRef(new Set<string>());
    const pendingAnswersRef = useRef(new Map<string, SurveyAnswerRequest>());
    const saveQueuesRef = useRef(new Map<string, Promise<void>>());
    const debounceTimersRef = useRef(new Map<string, ReturnType<typeof setTimeout>>());
    const failedQuestionIdsRef = useRef(new Set<string>());
    const flushPromiseRef = useRef<Promise<void> | null>(null);
    const activeRequestCountRef = useRef(0);
    const requestWaitersRef = useRef<Array<() => void>>([]);
    const generationRef = useRef(0);
    const [savingQuestionCount, setSavingQuestionCount] = useState(0);
    const [failedQuestionCount, setFailedQuestionCount] = useState(0);

    const clearQuestionFailure = useCallback((questionId: string) => {
        if (!failedQuestionIdsRef.current.delete(questionId)) {
            return;
        }

        setFailedQuestionCount(failedQuestionIdsRef.current.size);
    }, []);

    const markQuestionFailed = useCallback((questionId: string) => {
        failedQuestionIdsRef.current.add(questionId);
        setFailedQuestionCount(failedQuestionIdsRef.current.size);
    }, []);

    const acquireRequestSlot = useCallback(async () => {
        if (activeRequestCountRef.current < MAX_PARALLEL_REQUESTS) {
            activeRequestCountRef.current += 1;
            return;
        }

        await new Promise<void>((resolve) => {
            requestWaitersRef.current.push(resolve);
        });
    }, []);

    const releaseRequestSlot = useCallback(() => {
        const nextWaiter = requestWaitersRef.current.shift();
        if (nextWaiter) {
            nextWaiter();
            return;
        }

        activeRequestCountRef.current = Math.max(0, activeRequestCountRef.current - 1);
    }, []);

    const withRequestSlot = useCallback(
        async <T>(request: () => Promise<T>) => {
            await acquireRequestSlot();
            try {
                return await request();
            } finally {
                releaseRequestSlot();
            }
        },
        [acquireRequestSlot, releaseRequestSlot],
    );

    const ensureResponse = useCallback(async () => {
        if (responseIdRef.current) {
            return responseIdRef.current;
        }

        if (!responsePromiseRef.current) {
            const generation = generationRef.current;
            responsePromiseRef.current = createSurveyResponse(surveyId)
                .then(({ id }) => {
                    if (generation === generationRef.current) {
                        responseIdRef.current = id;
                    }

                    return id;
                })
                .catch((error: unknown) => {
                    if (generation === generationRef.current) {
                        responsePromiseRef.current = null;
                    }
                    throw error;
                });
        }

        return responsePromiseRef.current;
    }, [surveyId]);

    function isEmptyPayload(payload: SurveyAnswerRequest) {
        const hasIds = payload.selectedAnswerOptionIds && payload.selectedAnswerOptionIds.length > 0;
        const hasText = payload.textValue && payload.textValue.trim() !== '';
        return !hasIds && !hasText;
    }

    const persistAnswer = useCallback(
        async (questionId: string, answerPayload: SurveyAnswerRequest, generation: number) => {
            await withRequestSlot(async () => {
                if (generation !== generationRef.current) {
                    return;
                }

                const isPersisted = persistedQuestionIdsRef.current.has(questionId);

                if (isEmptyPayload(answerPayload)) {
                    if (isPersisted) {
                        const responseId = await ensureResponse();
                        try {
                            await deleteSurveyAnswer(responseId, questionId);
                        } catch (error) {
                            if (!axios.isAxiosError(error) || error.response?.status !== 404) {
                                throw error;
                            }
                        }
                        if (generation === generationRef.current) {
                            persistedQuestionIdsRef.current.delete(questionId);
                        }
                    }

                    return;
                }

                const responseId = await ensureResponse();

                if (isPersisted) {
                    try {
                        await updateSurveyAnswer(responseId, questionId, answerPayload);
                        return;
                    } catch (error) {
                        if (!axios.isAxiosError(error) || error.response?.status !== 404) {
                            throw error;
                        }

                        if (generation !== generationRef.current) {
                            return;
                        }
                        persistedQuestionIdsRef.current.delete(questionId);
                    }
                }

                try {
                    await updateSurveyAnswer(responseId, questionId, answerPayload);
                } catch (error) {
                    if (!axios.isAxiosError(error) || error.response?.status !== 409) {
                        throw error;
                    }

                    await updateSurveyAnswer(responseId, questionId, answerPayload);
                }

                if (generation === generationRef.current) {
                    persistedQuestionIdsRef.current.add(questionId);
                }
            });
        },
        [ensureResponse, withRequestSlot],
    );

    const startQuestionQueue = useCallback(
        (questionId: string): Promise<void> | null => {
            const activeQueue = saveQueuesRef.current.get(questionId);
            if (activeQueue) {
                return activeQueue;
            }

            if (!pendingAnswersRef.current.has(questionId)) {
                return null;
            }

            const answerPayload = pendingAnswersRef.current.get(questionId);
            pendingAnswersRef.current.delete(questionId);
            setSavingQuestionCount((count) => count + 1);
            const generation = generationRef.current;
            if (answerPayload === undefined) {
                setSavingQuestionCount((count) => Math.max(0, count - 1));
                return null;
            }
            const queue = persistAnswer(questionId, answerPayload, generation)
                .then(() => {
                    if (generation === generationRef.current) {
                        clearQuestionFailure(questionId);
                    }
                })
                .catch((error: unknown) => {
                    if (generation === generationRef.current) {
                        if (!pendingAnswersRef.current.has(questionId)) {
                            pendingAnswersRef.current.set(questionId, answerPayload);
                        }
                        markQuestionFailed(questionId);
                    }
                    throw error;
                })
                .finally(() => {
                    if (saveQueuesRef.current.get(questionId) === queue) {
                        saveQueuesRef.current.delete(questionId);
                    }
                    if (generation !== generationRef.current) {
                        return;
                    }

                    setSavingQuestionCount((count) => Math.max(0, count - 1));
                    if (
                        pendingAnswersRef.current.has(questionId) &&
                        !debounceTimersRef.current.has(questionId) &&
                        !failedQuestionIdsRef.current.has(questionId)
                    ) {
                        void startQuestionQueue(questionId)?.catch(() => undefined);
                    }
                });

            saveQueuesRef.current.set(questionId, queue);
            return queue;
        },
        [clearQuestionFailure, markQuestionFailed, persistAnswer],
    );

    const scheduleAnswerSave = useCallback(
        (questionId: string, payload: SurveyAnswerRequest, options: ScheduleOptions = {}) => {
            if (disabled) {
                return;
            }

            pendingAnswersRef.current.set(questionId, payload);
            clearQuestionFailure(questionId);

            const currentTimer = debounceTimersRef.current.get(questionId);
            if (currentTimer) {
                clearTimeout(currentTimer);
                debounceTimersRef.current.delete(questionId);
            }

            if (options.debounce) {
                const timer = setTimeout(() => {
                    debounceTimersRef.current.delete(questionId);
                    void startQuestionQueue(questionId)?.catch(() => undefined);
                }, TEXT_ANSWER_DEBOUNCE_MS);
                debounceTimersRef.current.set(questionId, timer);
                return;
            }

            void startQuestionQueue(questionId)?.catch(() => undefined);
        },
        [clearQuestionFailure, disabled, startQuestionQueue],
    );

    const flushQuestion = useCallback(
        async (questionId: string) => {
            const generation = generationRef.current;

            while (generation === generationRef.current) {
                const timer = debounceTimersRef.current.get(questionId);
                if (timer) {
                    clearTimeout(timer);
                    debounceTimersRef.current.delete(questionId);
                }

                const queue = startQuestionQueue(questionId) ?? saveQueuesRef.current.get(questionId);
                if (!queue) {
                    return;
                }

                await queue;

                if (
                    !pendingAnswersRef.current.has(questionId) &&
                    !saveQueuesRef.current.has(questionId) &&
                    !debounceTimersRef.current.has(questionId)
                ) {
                    return;
                }
            }
        },
        [startQuestionQueue],
    );

    const flushPendingAnswers = useCallback((): Promise<void> => {
        if (flushPromiseRef.current) {
            return flushPromiseRef.current;
        }

        const generation = generationRef.current;
        const flushPromise = (async () => {
            while (generation === generationRef.current) {
                const questionIds = new Set([
                    ...pendingAnswersRef.current.keys(),
                    ...saveQueuesRef.current.keys(),
                    ...debounceTimersRef.current.keys(),
                ]);

                if (questionIds.size === 0) {
                    break;
                }

                await Promise.all([...questionIds].map((questionId) => flushQuestion(questionId)));
            }

            if (generation === generationRef.current && failedQuestionIdsRef.current.size > 0) {
                throw new Error('Не удалось сохранить ответы');
            }
        })().finally(() => {
            if (flushPromiseRef.current === flushPromise) {
                flushPromiseRef.current = null;
            }
        });

        flushPromiseRef.current = flushPromise;
        return flushPromise;
    }, [flushQuestion]);

    const retryFailedAnswers = useCallback(async () => {
        const questionIds = [...failedQuestionIdsRef.current];
        questionIds.forEach(clearQuestionFailure);
        const results = await Promise.allSettled(questionIds.map((questionId) => flushQuestion(questionId)));

        if (results.some((result) => result.status === 'rejected')) {
            throw new Error('Не удалось сохранить ответы');
        }
    }, [clearQuestionFailure, flushQuestion]);

    useEffect(() => {
        generationRef.current += 1;
        responseIdRef.current = null;
        responsePromiseRef.current = null;
        persistedQuestionIdsRef.current.clear();
        pendingAnswersRef.current.clear();
        saveQueuesRef.current.clear();
        failedQuestionIdsRef.current.clear();
        flushPromiseRef.current = null;
        debounceTimersRef.current.forEach((timer) => clearTimeout(timer));
        debounceTimersRef.current.clear();
        setSavingQuestionCount(0);
        setFailedQuestionCount(0);

        return () => {
            generationRef.current += 1;
            debounceTimersRef.current.forEach((timer) => clearTimeout(timer));
            debounceTimersRef.current.clear();
        };
    }, [surveyId]);

    return {
        ensureResponse,
        failedQuestionCount,
        flushPendingAnswers,
        flushQuestion,
        isSaving: savingQuestionCount > 0,
        retryFailedAnswers,
        scheduleAnswerSave,
    };
}
