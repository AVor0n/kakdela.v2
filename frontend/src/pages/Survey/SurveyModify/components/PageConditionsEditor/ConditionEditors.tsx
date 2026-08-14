import { Button, Select, createStaticDataProvider, type StaticDataFetcherItem } from '@hh.ru/magritte-ui';
import { CheckOutlinedSize24 } from '@hh.ru/magritte-ui/icon';
import { useEffect, useId, useMemo, useRef, useState, type FormEvent, type ReactNode } from 'react';
import {
    addConditionAtom,
    addConditionNode,
    deleteConditionNode,
    updateConditionAtom,
    updateConditionNode,
    type ConditionAtomRequest,
} from '@/api/conditions';
import type { Condition, ConditionLinkOperator, ConditionNode } from '@/shared/types/Condition.type';
import type { Question } from '@/shared/types/Question.type';
import type { Page } from '@/shared/types/Survey.type';
import style from './PageConditionsEditor.module.css';

type AtomDisplayOperator = 'EQUALS' | 'NOT_EQUALS';

export function getSupportedQuestions(page: Page) {
    return page.questions.filter(
        (question) =>
            question.type === 'YES_NO' ||
            ((question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') &&
                question.answerOptions.length > 0),
    );
}

export function getPlainText(value: string) {
    return value
        .replace(/<[^>]*>/g, ' ')
        .replace(/\s+/g, ' ')
        .trim();
}

function getQuestion(page: Page, questionId?: string) {
    return page.questions.find(({ id }) => id === questionId);
}

const DISPLAY_OPERATOR_OPTIONS: StaticDataFetcherItem[] = [
    { value: 'EQUALS', text: 'равно' },
    { value: 'NOT_EQUALS', text: 'не равно' },
];
const BOOLEAN_OPTIONS: StaticDataFetcherItem[] = [
    { value: 'true', text: 'Да' },
    { value: 'false', text: 'Нет' },
];

function buildAtomRequest(question: Question, value: string, operator: AtomDisplayOperator): ConditionAtomRequest {
    const baseRequest = { questionId: question.id, operator: 'EQUALS' as const, isNegative: operator === 'NOT_EQUALS' };
    return question.type === 'YES_NO'
        ? { ...baseRequest, requiredBooleanValue: value === 'true' }
        : { ...baseRequest, requiredAnswerOptionId: value };
}

function getInitialAtomValue(question?: Question, node?: ConditionNode) {
    if (!question) return '';
    if (question.type === 'YES_NO') return String(node?.atom?.requiredBooleanValue ?? true);
    if (node?.atom?.requiredAnswerOptionId) return node.atom.requiredAnswerOptionId;
    return question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE'
        ? (question.answerOptions[0]?.id ?? '')
        : '';
}

export function ConditionSelect({
    disabled,
    name,
    options,
    title,
    value,
    onChange,
}: {
    disabled?: boolean;
    name: string;
    options: StaticDataFetcherItem[];
    title: string;
    value: string;
    onChange: (_value: string) => void;
}) {
    const dataProvider = useMemo(() => createStaticDataProvider(options, title), [options, title]);
    return (
        <Select
            type='label'
            name={name}
            value={options.find((option) => option.value === value)}
            dataProvider={dataProvider}
            triggerProps={{ disabled, stretched: true }}
            widthEqualToActivator
            onChange={(option) => onChange(option.value)}
        />
    );
}

export function AtomEditor({
    additionalActions,
    page,
    initialNode,
    disabled,
    deleteDisabled,
    onCancel,
    onDelete,
    onSubmit,
}: {
    additionalActions?: ReactNode;
    page: Page;
    initialNode?: ConditionNode;
    disabled?: boolean;
    deleteDisabled?: boolean;
    onCancel?: () => void;
    onDelete?: () => void;
    onSubmit: (_request: ConditionAtomRequest) => Promise<void>;
}) {
    const fieldId = useId();
    const questions = useMemo(() => getSupportedQuestions(page), [page]);
    const initialQuestion = getQuestion(page, initialNode?.atom?.questionId) ?? questions[0];
    const [questionId, setQuestionId] = useState(initialQuestion?.id ?? '');
    const [value, setValue] = useState(() => getInitialAtomValue(initialQuestion, initialNode));
    const [displayOperator, setDisplayOperator] = useState<AtomDisplayOperator>(
        initialNode?.operator === 'NOT_ATOM' ? 'NOT_EQUALS' : 'EQUALS',
    );
    const serverStateSignature = JSON.stringify({ operator: initialNode?.operator, atom: initialNode?.atom });
    const supportedQuestionsSignature = JSON.stringify(
        questions.map((item) => ({
            id: item.id,
            type: item.type,
            optionIds:
                item.type === 'SINGLE_CHOICE' || item.type === 'MULTIPLE_CHOICE'
                    ? item.answerOptions.map(({ id }) => id)
                    : [],
        })),
    );
    const syncSignature = `${serverStateSignature}:${supportedQuestionsSignature}`;
    const previousSyncSignature = useRef(syncSignature);
    const question = getQuestion(page, questionId);
    const savedQuestion = getQuestion(page, initialNode?.atom?.questionId);
    const savedOperator = initialNode?.operator === 'NOT_ATOM' ? 'NOT_EQUALS' : 'EQUALS';
    const isDirty =
        !initialNode ||
        questionId !== (savedQuestion?.id ?? '') ||
        value !== getInitialAtomValue(savedQuestion, initialNode) ||
        displayOperator !== savedOperator;

    useEffect(() => {
        if (previousSyncSignature.current === syncSignature) return;
        previousSyncSignature.current = syncSignature;
        const nextQuestion = getQuestion(page, initialNode?.atom?.questionId) ?? questions[0];
        setQuestionId(nextQuestion?.id ?? '');
        setValue(getInitialAtomValue(nextQuestion, initialNode));
        setDisplayOperator(initialNode?.operator === 'NOT_ATOM' ? 'NOT_EQUALS' : 'EQUALS');
    }, [initialNode, page, questions, syncSignature]);

    const questionOptions = useMemo(
        () =>
            questions.map((item) => ({
                value: item.id,
                text: getPlainText(item.text) || `Вопрос ${item.serialNumber}`,
            })),
        [questions],
    );
    const valueOptions = useMemo(() => {
        if (question?.type === 'YES_NO') return BOOLEAN_OPTIONS;
        return question?.type === 'SINGLE_CHOICE' || question?.type === 'MULTIPLE_CHOICE'
            ? question.answerOptions.map((option) => ({
                  value: option.id,
                  text: getPlainText(option.text) || `Вариант ${option.serialNumber}`,
              }))
            : [];
    }, [question]);

    if (questions.length === 0) {
        return <p className={style.hint}>Добавьте вопрос «Да / Нет» или вопрос с вариантами ответа.</p>;
    }

    return (
        <form
            className={style.atomForm}
            onSubmit={(event: FormEvent) => {
                event.preventDefault();
                if (question && value) void onSubmit(buildAtomRequest(question, value, displayOperator));
            }}
        >
            <div className={style.atomQuestion}>
                <ConditionSelect
                    name={`${fieldId}-question`}
                    title='Вопрос'
                    value={questionId}
                    options={questionOptions}
                    disabled={disabled}
                    onChange={(nextQuestionId) => {
                        setQuestionId(nextQuestionId);
                        setValue(getInitialAtomValue(getQuestion(page, nextQuestionId)));
                    }}
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

export function TreeNodeEditor({
    condition,
    node,
    page,
    depth,
    disabled,
    mutateAndRefresh,
}: {
    condition: Condition;
    node: ConditionNode;
    page: Page;
    depth: number;
    disabled: boolean;
    mutateAndRefresh: (_mutation: () => Promise<unknown>) => Promise<void>;
}) {
    const [isAddingAtom, setIsAddingAtom] = useState(false);
    if (node.operator === 'ATOM' || node.operator === 'NOT_ATOM') {
        return (
            <div className={style.conditionRow}>
                <AtomEditor
                    additionalActions={
                        <div className={style.wrapActions}>
                            <span className={style.wrapActionsLabel}>Объединить с новым условием:</span>
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
                    }
                    page={page}
                    initialNode={node}
                    disabled={disabled}
                    onDelete={() => void mutateAndRefresh(() => deleteConditionNode(node.id))}
                    onSubmit={(request) => mutateAndRefresh(() => updateConditionAtom(node.id, request))}
                />
            </div>
        );
    }

    return (
        <div className={style.groupNode}>
            <div className={style.groupOperator}>
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
                    <option value='AND'>И</option>
                    <option value='OR'>ИЛИ</option>
                </select>
            </div>
            <div className={style.groupBranch}>
                <div className={style.groupHeader}>
                    <span className={style.groupTitle}>Группа условий</span>
                    <button
                        className={style.groupDeleteButton}
                        type='button'
                        disabled={disabled}
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
                                disabled={disabled || depth >= 3}
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
