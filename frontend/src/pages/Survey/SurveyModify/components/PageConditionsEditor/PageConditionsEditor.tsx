import { Button } from '@hh.ru/magritte-ui';
import { useMemo, useState, type FormEvent } from 'react';
import {
    addConditionAtom,
    addConditionNode,
    createCondition,
    deleteCondition,
    deleteConditionNode,
    getCondition,
    updateCondition,
    updateConditionAtom,
    updateConditionNode,
    type ConditionAtomRequest,
} from '@/api/conditions';
import {
    addPageCondition,
    deletePageCondition,
    replacePageCondition,
} from '@/entities/Pages/Pages.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { useAppSelector } from '@/hooks/useAppSelector';
import type { Condition, ConditionLinkOperator, ConditionNode } from '@/shared/types/Condition.type';
import type { Question } from '@/shared/types/Question.type';
import type { Page } from '@/shared/types/Survey.type';
import style from './PageConditionsEditor.module.css';

type Props = {
    page: Page;
};

type AtomEditorProps = {
    page: Page;
    initialNode?: ConditionNode;
    submitLabel: string;
    disabled?: boolean;
    onCancel?: () => void;
    onSubmit: (_request: ConditionAtomRequest) => Promise<void>;
};

type TreeNodeEditorProps = {
    condition: Condition;
    node: ConditionNode;
    page: Page;
    depth: number;
    disabled: boolean;
    mutateAndRefresh: (_mutation: () => Promise<unknown>) => Promise<void>;
};

function getSupportedQuestions(page: Page) {
    return page.questions.filter((question) => {
        if (question.type === 'YES_NO') return true;
        if (question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') {
            return question.answerOptions.length > 0;
        }
        return false;
    });
}

function getPlainText(value: string) {
    return value.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim();
}

function getQuestion(page: Page, questionId?: string) {
    return page.questions.find(({ id }) => id === questionId);
}

function buildAtomRequest(question: Question, value: string, isNegative: boolean): ConditionAtomRequest {
    if (question.type === 'YES_NO') {
        return {
            questionId: question.id,
            operator: 'EQUALS',
            requiredBooleanValue: value === 'true',
            isNegative,
        };
    }

    return {
        questionId: question.id,
        operator: 'EQUALS',
        requiredAnswerOptionId: value,
        isNegative,
    };
}

function getInitialAtomValue(question: Question | undefined, node?: ConditionNode) {
    if (!question) return '';
    if (question.type === 'YES_NO') return String(node?.atom?.requiredBooleanValue ?? true);
    if (node?.atom?.requiredAnswerOptionId) return node.atom.requiredAnswerOptionId;
    if (question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') {
        return question.answerOptions[0]?.id ?? '';
    }
    return '';
}

function AtomEditor({ page, initialNode, submitLabel, disabled, onCancel, onSubmit }: AtomEditorProps) {
    const questions = useMemo(() => getSupportedQuestions(page), [page]);
    const initialQuestion = getQuestion(page, initialNode?.atom?.questionId) ?? questions[0];
    const [questionId, setQuestionId] = useState(initialQuestion?.id ?? '');
    const [value, setValue] = useState(() => getInitialAtomValue(initialQuestion, initialNode));
    const [isNegative, setIsNegative] = useState(initialNode?.operator === 'NOT_ATOM');
    const question = getQuestion(page, questionId);

    const changeQuestion = (nextQuestionId: string) => {
        const nextQuestion = getQuestion(page, nextQuestionId);
        setQuestionId(nextQuestionId);
        setValue(getInitialAtomValue(nextQuestion));
    };

    const submit = (event: FormEvent) => {
        event.preventDefault();
        if (!question || !value) return;
        void onSubmit(buildAtomRequest(question, value, isNegative)).catch(() => undefined);
    };

    if (questions.length === 0) {
        return <p className={style.hint}>Добавьте вопрос «Да / Нет» или вопрос с вариантами ответа.</p>;
    }

    return (
        <form className={style.atomForm} onSubmit={submit}>
            <label className={style.field}>
                <span>Вопрос</span>
                <select value={questionId} disabled={disabled} onChange={(event) => changeQuestion(event.target.value)}>
                    {questions.map((item) => (
                        <option key={item.id} value={item.id}>
                            {getPlainText(item.text) || `Вопрос ${item.serialNumber}`}
                        </option>
                    ))}
                </select>
            </label>
            <label className={style.field}>
                <span>Значение</span>
                {question?.type === 'YES_NO' ? (
                    <select value={value} disabled={disabled} onChange={(event) => setValue(event.target.value)}>
                        <option value='true'>Да</option>
                        <option value='false'>Нет</option>
                    </select>
                ) : (
                    <select value={value} disabled={disabled} onChange={(event) => setValue(event.target.value)}>
                        {(question?.type === 'SINGLE_CHOICE' || question?.type === 'MULTIPLE_CHOICE') &&
                            question.answerOptions.map((option) => (
                                <option key={option.id} value={option.id}>
                                    {getPlainText(option.text) || `Вариант ${option.serialNumber}`}
                                </option>
                            ))}
                    </select>
                )}
            </label>
            <label className={style.negativeToggle}>
                <input
                    type='checkbox'
                    checked={isNegative}
                    disabled={disabled}
                    onChange={(event) => setIsNegative(event.target.checked)}
                />
                НЕ
            </label>
            <div className={style.formActions}>
                {onCancel && (
                    <Button mode='secondary' type='button' disabled={disabled} onClick={onCancel}>
                        Отмена
                    </Button>
                )}
                <Button mode='primary' type='submit' disabled={disabled || !value}>
                    {submitLabel}
                </Button>
            </div>
        </form>
    );
}

function AtomSummary({ page, node }: { page: Page; node: ConditionNode }) {
    const question = getQuestion(page, node.atom?.questionId);
    let value = '—';

    if (question?.type === 'YES_NO') {
        value = node.atom?.requiredBooleanValue ? 'Да' : 'Нет';
    } else if (question?.type === 'SINGLE_CHOICE' || question?.type === 'MULTIPLE_CHOICE') {
        value =
            getPlainText(
                question.answerOptions.find(({ id }) => id === node.atom?.requiredAnswerOptionId)?.text ?? '',
            ) || 'Удалённый вариант';
    }

    return (
        <span>
            {node.operator === 'NOT_ATOM' ? 'НЕ ' : ''}
            {question ? getPlainText(question.text) : 'Удалённый вопрос'} = {value}
        </span>
    );
}

function TreeNodeEditor({ condition, node, page, depth, disabled, mutateAndRefresh }: TreeNodeEditorProps) {
    const [isEditing, setIsEditing] = useState(false);
    const [isAddingAtom, setIsAddingAtom] = useState(false);
    const isLink = node.operator === 'AND' || node.operator === 'OR';

    if (!isLink) {
        return (
            <div className={style.atomNode}>
                {isEditing ? (
                    <AtomEditor
                        page={page}
                        initialNode={node}
                        submitLabel='Сохранить'
                        disabled={disabled}
                        onCancel={() => setIsEditing(false)}
                        onSubmit={async (request) => {
                            await mutateAndRefresh(() => updateConditionAtom(node.id, request));
                            setIsEditing(false);
                        }}
                    />
                ) : (
                    <>
                        <div className={style.nodeSummary}>
                            <AtomSummary page={page} node={node} />
                            <div className={style.inlineActions}>
                                <Button mode='secondary' type='button' disabled={disabled} onClick={() => setIsEditing(true)}>
                                    Изменить
                                </Button>
                                <Button
                                    mode='secondary'
                                    style='negative'
                                    type='button'
                                    disabled={disabled}
                                    onClick={() => void mutateAndRefresh(() => deleteConditionNode(node.id))}
                                >
                                    Удалить
                                </Button>
                            </div>
                        </div>
                        <div className={style.wrapActions}>
                            <span>Объединить с новым правилом:</span>
                            {(['AND', 'OR'] as const).map((operator) => (
                                <Button
                                    key={operator}
                                    mode='secondary'
                                    type='button'
                                    disabled={disabled || depth >= 3}
                                    onClick={() =>
                                        void mutateAndRefresh(() => addConditionNode(condition.id, node.id, operator))
                                    }
                                >
                                    {operator === 'AND' ? 'И' : 'ИЛИ'}
                                </Button>
                            ))}
                        </div>
                    </>
                )}
            </div>
        );
    }

    return (
        <div className={style.groupNode}>
            <div className={style.groupHeader}>
                <label>
                    Группа
                    <select
                        value={node.operator}
                        disabled={disabled}
                        onChange={(event) =>
                            void mutateAndRefresh(() =>
                                updateConditionNode(node.id, event.target.value as ConditionLinkOperator),
                            )
                        }
                    >
                        <option value='AND'>И</option>
                        <option value='OR'>ИЛИ</option>
                    </select>
                </label>
                <Button
                    mode='secondary'
                    style='negative'
                    type='button'
                    disabled={disabled}
                    onClick={() => void mutateAndRefresh(() => deleteConditionNode(node.id))}
                >
                    Удалить группу
                </Button>
            </div>
            <div className={style.children}>
                {node.children.map((child) => (
                    <TreeNodeEditor
                        key={child.id}
                        condition={condition}
                        node={child}
                        page={page}
                        depth={depth + 1}
                        disabled={disabled}
                        mutateAndRefresh={mutateAndRefresh}
                    />
                ))}
            </div>
            {isAddingAtom ? (
                <AtomEditor
                    page={page}
                    submitLabel='Добавить правило'
                    disabled={disabled}
                    onCancel={() => setIsAddingAtom(false)}
                    onSubmit={async (request) => {
                        await mutateAndRefresh(() =>
                            addConditionAtom(condition.id, { ...request, parentNodeId: node.id }),
                        );
                        setIsAddingAtom(false);
                    }}
                />
            ) : (
                <Button
                    mode='secondary'
                    type='button'
                    disabled={disabled || depth >= 3}
                    onClick={() => setIsAddingAtom(true)}
                >
                    Добавить правило в группу
                </Button>
            )}
        </div>
    );
}

export function PageConditionsEditor({ page }: Props) {
    const dispatch = useAppDispatch();
    const surveyPages = useAppSelector((state) => state.pages.pages);
    const [isExpanded, setIsExpanded] = useState(false);
    const [isCreating, setIsCreating] = useState(false);
    const [pendingConditionId, setPendingConditionId] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const forwardPages = surveyPages.filter(({ serialNumber }) => serialNumber > page.serialNumber);
    const firstForwardPageId = forwardPages[0]?.id ?? '';
    const [newNextPageId, setNewNextPageId] = useState(firstForwardPageId);

    const replaceCondition = (condition: Condition) => {
        dispatch(replacePageCondition({ pageId: page.id, condition }));
    };

    const refreshCondition = async (conditionId: string) => {
        replaceCondition(await getCondition(conditionId));
    };

    const mutateAndRefresh = async (conditionId: string, mutation: () => Promise<unknown>) => {
        setPendingConditionId(conditionId);
        setError(null);
        try {
            await mutation();
            await refreshCondition(conditionId);
        } catch {
            setError('Не удалось сохранить логику перехода');
            throw new Error('Condition mutation failed');
        } finally {
            setPendingConditionId(null);
        }
    };

    const createBranch = async (request: ConditionAtomRequest) => {
        if (!newNextPageId) return;
        setPendingConditionId('new');
        setError(null);
        let createdCondition: Condition | null = null;
        try {
            createdCondition = await createCondition(page.id, { nextPageId: newNextPageId });
            await addConditionAtom(createdCondition.id, request);
            dispatch(addPageCondition({ pageId: page.id, condition: await getCondition(createdCondition.id) }));
            setIsCreating(false);
        } catch {
            if (createdCondition) await deleteCondition(createdCondition.id).catch(() => undefined);
            setError('Не удалось создать условие перехода');
        } finally {
            setPendingConditionId(null);
        }
    };

    const removeCondition = async (conditionId: string) => {
        setPendingConditionId(conditionId);
        setError(null);
        try {
            await deleteCondition(conditionId);
            dispatch(deletePageCondition({ pageId: page.id, conditionId }));
        } catch {
            setError('Не удалось удалить условие перехода');
        } finally {
            setPendingConditionId(null);
        }
    };

    return (
        <section className={style.editor}>
            <button className={style.header} type='button' onClick={() => setIsExpanded((value) => !value)}>
                <span>Логика перехода</span>
                <span className={style.summary}>
                    {page.conditions.length > 0 ? `${page.conditions.length} усл.` : 'По порядку'} ·{' '}
                    {isExpanded ? 'Свернуть' : 'Настроить'}
                </span>
            </button>
            {isExpanded && (
                <div className={style.content}>
                    {page.conditions.length === 0 && !isCreating && (
                        <p className={style.hint}>Если условия не заданы, откроется следующая страница по порядку.</p>
                    )}
                    {page.conditions.map((condition) => {
                        const isPending = pendingConditionId === condition.id;
                        const isTargetInvalid = !forwardPages.some(({ id }) => id === condition.nextPageId);
                        return (
                            <article className={style.condition} key={condition.id}>
                                <div className={style.conditionHeader}>
                                    <strong>Если</strong>
                                    <label className={style.targetField}>
                                        перейти к
                                        <select
                                            value={condition.nextPageId}
                                            disabled={isPending}
                                            onChange={(event) => {
                                                const nextPageId = event.target.value;
                                                setPendingConditionId(condition.id);
                                                setError(null);
                                                void updateCondition(condition.id, { nextPageId })
                                                    .then(replaceCondition)
                                                    .catch(() => setError('Не удалось изменить следующую страницу'))
                                                    .finally(() => setPendingConditionId(null));
                                            }}
                                        >
                                            {isTargetInvalid && (
                                                <option value={condition.nextPageId} disabled>
                                                    Некорректный переход — выберите новую страницу
                                                </option>
                                            )}
                                            {forwardPages.map((targetPage) => (
                                                <option key={targetPage.id} value={targetPage.id}>
                                                    Страница {targetPage.serialNumber}
                                                    {targetPage.title ? ` — ${getPlainText(targetPage.title)}` : ''}
                                                </option>
                                            ))}
                                        </select>
                                    </label>
                                    <Button
                                        mode='secondary'
                                        style='negative'
                                        type='button'
                                        disabled={isPending}
                                        onClick={() => void removeCondition(condition.id)}
                                    >
                                        Удалить переход
                                    </Button>
                                </div>
                                {isTargetInvalid && (
                                    <p className={style.error}>После изменения порядка страниц переход ведёт назад.</p>
                                )}
                                {condition.root ? (
                                    <TreeNodeEditor
                                        condition={condition}
                                        node={condition.root}
                                        page={page}
                                        depth={1}
                                        disabled={isPending}
                                        mutateAndRefresh={(mutation) => mutateAndRefresh(condition.id, mutation)}
                                    />
                                ) : (
                                    <AtomEditor
                                        page={page}
                                        submitLabel='Добавить первое правило'
                                        disabled={isPending}
                                        onSubmit={(request) =>
                                            mutateAndRefresh(condition.id, () => addConditionAtom(condition.id, request))
                                        }
                                    />
                                )}
                            </article>
                        );
                    })}
                    {isCreating && (
                        <article className={style.condition}>
                            <div className={style.conditionHeader}>
                                <strong>Новое условие</strong>
                                <label className={style.targetField}>
                                    перейти к
                                    <select
                                        value={newNextPageId}
                                        disabled={pendingConditionId === 'new'}
                                        onChange={(event) => setNewNextPageId(event.target.value)}
                                    >
                                        {forwardPages.map((targetPage) => (
                                            <option key={targetPage.id} value={targetPage.id}>
                                                Страница {targetPage.serialNumber}
                                                {targetPage.title ? ` — ${getPlainText(targetPage.title)}` : ''}
                                            </option>
                                        ))}
                                    </select>
                                </label>
                            </div>
                            <AtomEditor
                                page={page}
                                submitLabel='Создать переход'
                                disabled={pendingConditionId === 'new'}
                                onCancel={() => setIsCreating(false)}
                                onSubmit={createBranch}
                            />
                        </article>
                    )}
                    {!isCreating && (
                        <Button
                            mode='secondary'
                            type='button'
                            disabled={forwardPages.length === 0 || getSupportedQuestions(page).length === 0}
                            onClick={() => {
                                setNewNextPageId(forwardPages[0]?.id ?? '');
                                setIsCreating(true);
                            }}
                        >
                            Добавить условие перехода
                        </Button>
                    )}
                    {forwardPages.length === 0 && <p className={style.hint}>Это последняя страница опроса.</p>}
                    {error && <p className={style.error}>{error}</p>}
                </div>
            )}
        </section>
    );
}
