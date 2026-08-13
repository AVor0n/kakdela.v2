import { Button, Checkbox, Select, createStaticDataProvider, type StaticDataFetcherItem } from '@hh.ru/magritte-ui';
import { CheckOutlinedSize24 } from '@hh.ru/magritte-ui/icon';
import { useEffect, useId, useMemo, useRef, useState, type ChangeEvent, type FormEvent, type ReactNode } from 'react';
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
import { addPageCondition, deletePageCondition, replacePageCondition } from '@/entities/Pages/Pages.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { useAppSelector } from '@/hooks/useAppSelector';
import type { Condition, ConditionLinkOperator, ConditionNode } from '@/shared/types/Condition.type';
import type { Question } from '@/shared/types/Question.type';
import type { Page } from '@/shared/types/Survey.type';
import { getApiError } from '@/shared/utils/apiError';
import { validateCondition, type ConditionValidationIssueCode } from '@/shared/utils/conditions';
import style from './PageConditionsEditor.module.css';

type Props = {
    page: Page;
};

type AtomEditorProps = {
    additionalActions?: ReactNode;
    page: Page;
    initialNode?: ConditionNode;
    disabled?: boolean;
    deleteDisabled?: boolean;
    onCancel?: () => void;
    onDelete?: () => void;
    onSubmit: (_request: ConditionAtomRequest) => Promise<void>;
};

type AtomDisplayOperator = 'EQUALS' | 'NOT_EQUALS';

type ConditionSelectProps = {
    disabled?: boolean;
    name: string;
    options: StaticDataFetcherItem[];
    title: string;
    value: string;
    onChange: (_value: string) => void;
};

type TreeNodeEditorProps = {
    condition: Condition;
    node: ConditionNode;
    page: Page;
    depth: number;
    disabled: boolean;
    mutateAndRefresh: (_mutation: () => Promise<unknown>) => Promise<void>;
};

type ConditionConflictState = {
    conditionIds: [string, string];
} | null;

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
    return value
        .replace(/<[^>]*>/g, ' ')
        .replace(/\s+/g, ' ')
        .trim();
}

function getQuestion(page: Page, questionId?: string) {
    return page.questions.find(({ id }) => id === questionId);
}

const CONDITION_VALIDATION_MESSAGES: Record<ConditionValidationIssueCode, string> = {
    MISSING_ROOT: 'Добавьте хотя бы одно правило.',
    INVALID_ATOM: 'Заполните вопрос, оператор и значение.',
    INCOMPLETE_GROUP: 'Добавьте как минимум два правила в группу.',
    TARGET_PAGE_NOT_FOUND: 'Выберите существующую страницу перехода.',
    TARGET_PAGE_NOT_FORWARD: 'Переход должен вести на следующую страницу.',
};

const DISPLAY_OPERATOR_OPTIONS: StaticDataFetcherItem[] = [
    { value: 'EQUALS', text: 'равно' },
    { value: 'NOT_EQUALS', text: 'не равно' },
];

const BOOLEAN_OPTIONS: StaticDataFetcherItem[] = [
    { value: 'true', text: 'Да' },
    { value: 'false', text: 'Нет' },
];

function buildAtomRequest(
    question: Question,
    value: string,
    displayOperator: AtomDisplayOperator,
): ConditionAtomRequest {
    const isNegative = displayOperator === 'NOT_EQUALS';
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

function ConditionSelect({ disabled, name, options, title, value, onChange }: ConditionSelectProps) {
    const selectedValue = options.find((option) => option.value === value);
    const dataProvider = useMemo(() => createStaticDataProvider(options, title), [options, title]);

    return (
        <Select
            type='label'
            name={name}
            value={selectedValue}
            dataProvider={dataProvider}
            triggerProps={{ disabled, stretched: true }}
            widthEqualToActivator
            onChange={(option) => onChange(option.value)}
        />
    );
}

function AtomEditor({
    additionalActions,
    page,
    initialNode,
    disabled,
    deleteDisabled,
    onCancel,
    onDelete,
    onSubmit,
}: AtomEditorProps) {
    const fieldId = useId();
    const questions = useMemo(() => getSupportedQuestions(page), [page]);
    const initialQuestion = getQuestion(page, initialNode?.atom?.questionId) ?? questions[0];
    const [questionId, setQuestionId] = useState(initialQuestion?.id ?? '');
    const [value, setValue] = useState(() => getInitialAtomValue(initialQuestion, initialNode));
    const [displayOperator, setDisplayOperator] = useState<AtomDisplayOperator>(
        initialNode?.operator === 'NOT_ATOM' ? 'NOT_EQUALS' : 'EQUALS',
    );
    const serverAtomSignature = JSON.stringify({ operator: initialNode?.operator, atom: initialNode?.atom });
    const previousServerAtomSignature = useRef(serverAtomSignature);
    const question = getQuestion(page, questionId);
    const questionOptions = useMemo<StaticDataFetcherItem[]>(
        () =>
            questions.map((item) => ({
                value: item.id,
                text: getPlainText(item.text) || `Вопрос ${item.serialNumber}`,
            })),
        [questions],
    );
    const valueOptions = useMemo<StaticDataFetcherItem[]>(() => {
        if (question?.type === 'YES_NO') return BOOLEAN_OPTIONS;
        if (question?.type === 'SINGLE_CHOICE' || question?.type === 'MULTIPLE_CHOICE') {
            return question.answerOptions.map((option) => ({
                value: option.id,
                text: getPlainText(option.text) || `Вариант ${option.serialNumber}`,
            }));
        }
        return [];
    }, [question]);
    const savedQuestion = getQuestion(page, initialNode?.atom?.questionId);
    const savedQuestionId = savedQuestion?.id ?? '';
    const savedValue = getInitialAtomValue(savedQuestion, initialNode);
    const savedDisplayOperator: AtomDisplayOperator = initialNode?.operator === 'NOT_ATOM' ? 'NOT_EQUALS' : 'EQUALS';
    const isDirty =
        !initialNode ||
        questionId !== savedQuestionId ||
        value !== savedValue ||
        displayOperator !== savedDisplayOperator;

    useEffect(() => {
        if (previousServerAtomSignature.current === serverAtomSignature) return;
        previousServerAtomSignature.current = serverAtomSignature;
        const nextQuestion = getQuestion(page, initialNode?.atom?.questionId) ?? questions[0];
        setQuestionId(nextQuestion?.id ?? '');
        setValue(getInitialAtomValue(nextQuestion, initialNode));
        setDisplayOperator(initialNode?.operator === 'NOT_ATOM' ? 'NOT_EQUALS' : 'EQUALS');
    }, [initialNode, page, questions, serverAtomSignature]);

    const changeQuestion = (nextQuestionId: string) => {
        const nextQuestion = getQuestion(page, nextQuestionId);
        setQuestionId(nextQuestionId);
        setValue(getInitialAtomValue(nextQuestion));
    };

    const submit = (event: FormEvent) => {
        event.preventDefault();
        if (!question || !value) return;
        void onSubmit(buildAtomRequest(question, value, displayOperator)).catch(() => undefined);
    };

    if (questions.length === 0) {
        return <p className={style.hint}>Добавьте вопрос «Да / Нет» или вопрос с вариантами ответа.</p>;
    }

    return (
        <form className={style.atomForm} onSubmit={submit}>
            <div className={style.atomQuestion}>
                <ConditionSelect
                    name={`${fieldId}-question`}
                    title='Вопрос'
                    value={questionId}
                    options={questionOptions}
                    disabled={disabled}
                    onChange={changeQuestion}
                />
            </div>
            <div className={style.atomOperator}>
                <ConditionSelect
                    name={`${fieldId}-operator`}
                    title='Оператор'
                    value={displayOperator}
                    options={DISPLAY_OPERATOR_OPTIONS}
                    disabled={disabled}
                    onChange={(operator) => setDisplayOperator(operator as AtomDisplayOperator)}
                />
            </div>
            <div className={style.atomValue}>
                <ConditionSelect
                    name={`${fieldId}-value`}
                    title='Значение'
                    value={value}
                    options={valueOptions}
                    disabled={disabled}
                    onChange={setValue}
                />
            </div>
            <div className={`${style.formActions} ${additionalActions ? style.formActionsWithAdditional : ''}`}>
                {onCancel && (
                    <Button mode='secondary' type='button' disabled={disabled} onClick={onCancel}>
                        Отмена
                    </Button>
                )}
                {isDirty || !onDelete ? (
                    <Button
                        mode='secondary'
                        style='accent'
                        type='submit'
                        disabled={disabled || !value}
                        icon={<CheckOutlinedSize24 />}
                        aria-label='Сохранить условие'
                    />
                ) : (
                    <Button
                        mode='secondary'
                        style='neutral'
                        type='button'
                        disabled={disabled || deleteDisabled}
                        icon={<img className={style.trashIcon} src='/trash.svg' alt='' />}
                        aria-label='Удалить условие'
                        onClick={onDelete}
                    />
                )}
            </div>
            {additionalActions && <div className={style.additionalActions}>{additionalActions}</div>}
        </form>
    );
}

function TreeNodeEditor({ condition, node, page, depth, disabled, mutateAndRefresh }: TreeNodeEditorProps) {
    const [isAddingAtom, setIsAddingAtom] = useState(false);
    const isLink = node.operator === 'AND' || node.operator === 'OR';

    if (!isLink) {
        return (
            <div className={style.conditionRow}>
                <AtomEditor
                    additionalActions={
                        <div className={style.wrapActions}>
                            <span className={style.wrapActionsLabel}>Объединить с новым правилом:</span>
                            {(['AND', 'OR'] as const).map((operator) => (
                                <Button
                                    key={operator}
                                    mode='secondary'
                                    type='button'
                                    disabled={disabled || condition.isActive || depth >= 3}
                                    onClick={() =>
                                        void mutateAndRefresh(() => addConditionNode(condition.id, node.id, operator))
                                    }
                                >
                                    {operator === 'AND' ? 'И' : 'ИЛИ'}
                                </Button>
                            ))}
                        </div>
                    }
                    page={page}
                    initialNode={node}
                    disabled={disabled}
                    deleteDisabled={condition.isActive}
                    onDelete={() => void mutateAndRefresh(() => deleteConditionNode(node.id))}
                    onSubmit={(request) => mutateAndRefresh(() => updateConditionAtom(node.id, request))}
                />
            </div>
        );
    }

    return (
        <div className={style.groupNode}>
            <div className={style.groupOperator}>
                <label>
                    <select
                        aria-label='Оператор группы'
                        value={node.operator}
                        disabled={disabled}
                        onChange={(event) =>
                            void mutateAndRefresh(() =>
                                updateConditionNode(node.id, event.target.value as ConditionLinkOperator),
                            )
                        }
                    >
                        <option value='AND'>AND</option>
                        <option value='OR'>OR</option>
                    </select>
                </label>
            </div>
            <div className={style.groupBranch}>
                <div className={style.groupHeader}>
                    <span className={style.groupTitle}>Группа условий</span>
                    <button
                        className={style.groupDeleteButton}
                        type='button'
                        disabled={disabled || condition.isActive}
                        aria-label='Удалить группу'
                        onClick={() => void mutateAndRefresh(() => deleteConditionNode(node.id))}
                    >
                        <img className={style.groupDeleteIcon} src='/X.svg' alt='' />
                    </button>
                </div>
                <div className={style.children}>
                    {node.children.map((child) => (
                        <div className={style.treeChild} key={child.id}>
                            <TreeNodeEditor
                                condition={condition}
                                node={child}
                                page={page}
                                depth={depth + 1}
                                disabled={disabled}
                                mutateAndRefresh={mutateAndRefresh}
                            />
                        </div>
                    ))}
                    <div className={`${style.treeChild} ${style.addTreeChild}`}>
                        {isAddingAtom ? (
                            <AtomEditor
                                page={page}
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
                                disabled={disabled || condition.isActive || depth >= 3}
                                onClick={() => setIsAddingAtom(true)}
                            >
                                +
                            </Button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export function PageConditionsEditor({ page }: Props) {
    const dispatch = useAppDispatch();
    const surveyPages = useAppSelector((state) => state.pages.pages);
    const [isCreating, setIsCreating] = useState(false);
    const [pendingConditionId, setPendingConditionId] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [conflict, setConflict] = useState<ConditionConflictState>(null);
    const forwardPages = useMemo(
        () => surveyPages.filter(({ serialNumber }) => serialNumber > page.serialNumber),
        [page.serialNumber, surveyPages],
    );
    const forwardPageOptions = useMemo<StaticDataFetcherItem[]>(
        () =>
            forwardPages.map((targetPage) => ({
                value: targetPage.id,
                text: `Страница ${targetPage.serialNumber}${
                    targetPage.title ? ` — ${getPlainText(targetPage.title)}` : ''
                }`,
            })),
        [forwardPages],
    );
    const firstForwardPageId = forwardPages[0]?.id ?? '';
    const [newNextPageId, setNewNextPageId] = useState(firstForwardPageId);

    const replaceCondition = (condition: Condition) => {
        dispatch(replacePageCondition({ pageId: page.id, condition }));
    };

    const refreshCondition = async (conditionId: string) => {
        const refreshedCondition = await getCondition(conditionId);
        replaceCondition(refreshedCondition);

        if (refreshedCondition.isActive) {
            const [validationIssue] = validateCondition(refreshedCondition, page, surveyPages);
            if (validationIssue) {
                setError(CONDITION_VALIDATION_MESSAGES[validationIssue.code]);
            }
        }
    };

    const handleMutationError = (requestError: unknown, fallbackMessage: string) => {
        const apiError = getApiError(requestError);
        if (
            apiError?.internalErrorCode === 'CONDITIONS_OF_PAGE_HAVE_CONFLICTS' &&
            apiError.object1Id &&
            apiError.object2Id
        ) {
            setConflict({
                conditionIds: [apiError.object1Id, apiError.object2Id],
            });
            return;
        }

        setError(apiError?.message || fallbackMessage);
    };

    const mutateAndRefresh = async (conditionId: string, mutation: () => Promise<unknown>) => {
        setPendingConditionId(conditionId);
        setError(null);
        setConflict(null);
        try {
            await mutation();
            await refreshCondition(conditionId);
        } catch (requestError) {
            handleMutationError(requestError, 'Не удалось сохранить логику перехода');
            await refreshCondition(conditionId).catch(() => undefined);
            throw requestError;
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
            createdCondition = await createCondition(page.id, { nextPageId: newNextPageId, isActive: false });
            await addConditionAtom(createdCondition.id, request);
            dispatch(addPageCondition({ pageId: page.id, condition: await getCondition(createdCondition.id) }));
            setIsCreating(false);
        } catch (requestError) {
            if (createdCondition) await deleteCondition(createdCondition.id).catch(() => undefined);
            handleMutationError(requestError, 'Не удалось создать условие перехода');
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
            <div className={style.header}>
                <span>Логика перехода</span>
                <span className={style.summary}>
                    {page.conditions.length > 0
                        ? `${page.conditions.filter(({ isActive }) => isActive).length} акт. из ${page.conditions.length}`
                        : 'По порядку'}
                </span>
            </div>
            <div className={style.content}>
                {page.conditions.length === 0 && !isCreating && (
                    <p className={style.hint}>Если условия не заданы, откроется следующая страница по порядку.</p>
                )}
                {page.conditions.map((condition) => {
                    const isPending = pendingConditionId === condition.id;
                    const isTargetInvalid = !forwardPages.some(({ id }) => id === condition.nextPageId);
                    const hasConflict = conflict?.conditionIds.includes(condition.id) ?? false;
                    const [activationIssue] = condition.isActive ? [] : validateCondition(condition, page, surveyPages);
                    const activationBlockReason = activationIssue
                        ? CONDITION_VALIDATION_MESSAGES[activationIssue.code]
                        : null;
                    const targetPageOptions: StaticDataFetcherItem[] = isTargetInvalid
                        ? [
                              {
                                  value: condition.nextPageId,
                                  text: 'Некорректный переход — выберите новую страницу',
                                  disabled: true,
                              },
                              ...forwardPageOptions,
                          ]
                        : forwardPageOptions;
                    return (
                        <article
                            className={`${style.condition} ${hasConflict ? style.conditionConflict : ''}`}
                            key={condition.id}
                        >
                            <div className={style.conditionHeader}>
                                <strong>Если</strong>
                                <div className={style.targetField}>
                                    <span>перейти к</span>
                                    <ConditionSelect
                                        name={`condition-target-${condition.id}`}
                                        title='Страница перехода'
                                        value={condition.nextPageId}
                                        options={targetPageOptions}
                                        disabled={isPending}
                                        onChange={(nextPageId) => {
                                            void mutateAndRefresh(condition.id, () =>
                                                updateCondition(condition.id, {
                                                    nextPageId,
                                                    isActive: condition.isActive,
                                                }),
                                            ).catch(() => undefined);
                                        }}
                                    />
                                </div>
                                <label className={style.activeField}>
                                    <Checkbox
                                        checked={condition.isActive}
                                        disabled={isPending || (!condition.isActive && activationBlockReason !== null)}
                                        onChange={(event: ChangeEvent<HTMLInputElement>) => {
                                            const isActive = event.target.checked;
                                            void mutateAndRefresh(condition.id, () =>
                                                updateCondition(condition.id, {
                                                    nextPageId: condition.nextPageId,
                                                    isActive,
                                                }),
                                            ).catch(() => undefined);
                                        }}
                                    />
                                    <span>Активно</span>
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
                            {!condition.isActive && activationBlockReason && (
                                <p className={style.hint}>Условие нельзя активировать: {activationBlockReason}</p>
                            )}
                            {condition.isActive && (
                                <p className={style.hint}>
                                    Чтобы добавить или удалить правила, сначала отключите условие.
                                </p>
                            )}
                            {isTargetInvalid && (
                                <p className={style.error}>После изменения порядка страниц переход ведёт назад.</p>
                            )}
                            {condition.root ? (
                                <div className={style.treeRoot}>
                                    <TreeNodeEditor
                                        condition={condition}
                                        node={condition.root}
                                        page={page}
                                        depth={1}
                                        disabled={isPending}
                                        mutateAndRefresh={(mutation) => mutateAndRefresh(condition.id, mutation)}
                                    />
                                </div>
                            ) : (
                                <AtomEditor
                                    page={page}
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
                            <div className={style.targetField}>
                                <span>перейти к</span>
                                <ConditionSelect
                                    name={`new-condition-target-${page.id}`}
                                    title='Страница перехода'
                                    value={newNextPageId}
                                    options={forwardPageOptions}
                                    disabled={pendingConditionId === 'new'}
                                    onChange={setNewNextPageId}
                                />
                            </div>
                        </div>
                        <AtomEditor
                            page={page}
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
        </section>
    );
}
