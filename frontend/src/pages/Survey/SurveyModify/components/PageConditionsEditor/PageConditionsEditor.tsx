import { Button, Checkbox, type StaticDataFetcherItem } from '@hh.ru/magritte-ui';
import { useMemo, useState, type ChangeEvent } from 'react';
import {
    addConditionAtom,
    createCondition,
    deleteCondition,
    getCondition,
    updateCondition,
    type ConditionAtomRequest,
} from '@/api/conditions';
import { deletePageCondition, upsertPageCondition } from '@/entities/Pages/Pages.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { useAppSelector } from '@/hooks/useAppSelector';
import type { Condition } from '@/shared/types/Condition.type';
import type { Page } from '@/shared/types/Survey.type';
import { getApiError } from '@/shared/utils/apiError';
import { validateCondition, type ConditionValidationIssueCode } from '@/shared/utils/conditions';
import { findActiveConditionWithTarget } from '@/shared/utils/conditionReferences';
import { AtomEditor, ConditionSelect, TreeNodeEditor, getPlainText, getSupportedQuestions } from './ConditionEditors';
import style from './PageConditionsEditor.module.css';

type Props = {
    page: Page;
};

type ConditionIssueState = {
    type: 'logical-conflict' | 'duplicate-target';
    conditionIds: string[];
} | null;

const CONDITION_VALIDATION_MESSAGES: Record<ConditionValidationIssueCode, string> = {
    MISSING_ROOT: 'Добавьте хотя бы одно условие.',
    INVALID_ATOM: 'Заполните вопрос, оператор и значение.',
    INCOMPLETE_GROUP: 'Добавьте как минимум два условия в группу.',
    TARGET_PAGE_NOT_FOUND: 'Выберите существующую страницу перехода.',
    TARGET_PAGE_NOT_FORWARD: 'Переход должен вести на следующую страницу.',
};
const LOGICAL_CONFLICT_MESSAGE =
    'Обнаружен конфликт правил. Измените выделенные правила, чтобы они не могли выполняться одновременно.';
const DUPLICATE_TARGET_REASON = 'Для выбранной страницы перехода уже существует активное правило.';
const DUPLICATE_TARGET_MESSAGE =
    'Для выбранной страницы перехода уже существует активное правило. Сначала измените или отключите выделенное правило.';

export function PageConditionsEditor({ page }: Props) {
    const dispatch = useAppDispatch();
    const surveyPages = useAppSelector((state) => state.pages.pages);
    const [isCreating, setIsCreating] = useState(false);
    const [pendingConditionIds, setPendingConditionIds] = useState<Set<string>>(() => new Set());
    const [error, setError] = useState<string | null>(null);
    const [conditionIssue, setConditionIssue] = useState<ConditionIssueState>(null);
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
        dispatch(upsertPageCondition({ pageId: page.id, condition }));
    };

    const setConditionPending = (conditionId: string, isPending: boolean) => {
        setPendingConditionIds((currentIds) => {
            const nextIds = new Set(currentIds);
            if (isPending) nextIds.add(conditionId);
            else nextIds.delete(conditionId);
            return nextIds;
        });
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

    const handleMutationError = (requestError: unknown, fallbackMessage: string, conditionId?: string) => {
        const apiError = getApiError(requestError);
        if (apiError?.internalErrorCode === 'CONDITIONS_OF_PAGE_HAVE_CONFLICTS') {
            setError(LOGICAL_CONFLICT_MESSAGE);
            if (apiError.object1Id && apiError.object2Id) {
                setConditionIssue({
                    type: 'logical-conflict',
                    conditionIds: [apiError.object1Id, apiError.object2Id],
                });
            }
            return;
        }

        if (apiError?.internalErrorCode === 'CONDITION_FOR_THESE_PAGE_AND_NEXT_PAGE_ALREADY_EXISTS') {
            setError(DUPLICATE_TARGET_MESSAGE);
            if (apiError.object1Id && conditionId) {
                setConditionIssue({
                    type: 'duplicate-target',
                    conditionIds: [conditionId, apiError.object1Id],
                });
            }
            return;
        }

        setError(apiError?.message || fallbackMessage);
    };

    const mutateAndRefresh = async (conditionId: string, mutation: () => Promise<unknown>) => {
        setConditionPending(conditionId, true);
        setError(null);
        setConditionIssue(null);
        try {
            await mutation();
            await refreshCondition(conditionId);
        } catch (requestError) {
            handleMutationError(requestError, 'Не удалось сохранить логику перехода', conditionId);
            await refreshCondition(conditionId).catch(() => undefined);
            throw requestError;
        } finally {
            setConditionPending(conditionId, false);
        }
    };

    const mutateTreeAndRefresh = (condition: Condition, mutation: () => Promise<unknown>) =>
        mutateAndRefresh(condition.id, async () => {
            if (condition.isActive) {
                await updateCondition(condition.id, {
                    nextPageId: condition.nextPageId,
                    isActive: false,
                });
            }
            await mutation();
        });

    const createBranch = async (request: ConditionAtomRequest) => {
        if (!newNextPageId) return;
        setConditionPending('new', true);
        setError(null);
        setConditionIssue(null);
        let createdCondition: Condition | null = null;
        try {
            createdCondition = await createCondition(page.id, { nextPageId: newNextPageId, isActive: false });
            await addConditionAtom(createdCondition.id, request);
            dispatch(upsertPageCondition({ pageId: page.id, condition: await getCondition(createdCondition.id) }));
            setIsCreating(false);
        } catch (requestError) {
            if (createdCondition) await deleteCondition(createdCondition.id).catch(() => undefined);
            handleMutationError(requestError, 'Не удалось создать правило перехода');
        } finally {
            setConditionPending('new', false);
        }
    };

    const removeCondition = async (conditionId: string) => {
        setConditionPending(conditionId, true);
        setError(null);
        setConditionIssue(null);
        try {
            await deleteCondition(conditionId);
            dispatch(deletePageCondition({ pageId: page.id, conditionId }));
        } catch {
            setError('Не удалось удалить правило перехода');
        } finally {
            setConditionPending(conditionId, false);
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
                    <p className={style.hint}>Если правила не заданы, откроется следующая страница по порядку.</p>
                )}
                {page.conditions.map((condition) => {
                    const isPending = pendingConditionIds.has(condition.id);
                    const isTargetInvalid = !forwardPages.some(({ id }) => id === condition.nextPageId);
                    const hasIssue = conditionIssue?.conditionIds.includes(condition.id) ?? false;
                    const [activationIssue] = condition.isActive ? [] : validateCondition(condition, page, surveyPages);
                    const duplicateActiveCondition = findActiveConditionWithTarget(
                        page.conditions,
                        condition.nextPageId,
                        condition.id,
                    );
                    const activationBlockReason = activationIssue
                        ? CONDITION_VALIDATION_MESSAGES[activationIssue.code]
                        : duplicateActiveCondition
                          ? DUPLICATE_TARGET_REASON
                          : null;
                    const availableTargetOptions = forwardPageOptions.map((option) =>
                        findActiveConditionWithTarget(page.conditions, option.value, condition.id)
                            ? {
                                  ...option,
                                  text: `${String(option.text)} (используется активным правилом)`,
                              }
                            : option,
                    );
                    const targetPageOptions: StaticDataFetcherItem[] = isTargetInvalid
                        ? [
                              {
                                  value: condition.nextPageId,
                                  text: 'Некорректный переход — выберите новую страницу',
                                  disabled: true,
                              },
                              ...availableTargetOptions,
                          ]
                        : availableTargetOptions;
                    return (
                        <article
                            className={`${style.condition} ${hasIssue ? style.conditionConflict : ''}`}
                            key={condition.id}
                        >
                            <div className={style.conditionHeader}>
                                <div className={style.targetField}>
                                    <span>Перейти к</span>
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
                                                    isActive: false,
                                                }),
                                            ).catch(() => undefined);
                                        }}
                                    />
                                </div>
                                <div className={style.inlineActions}>
                                    <label className={style.activeField}>
                                        <Checkbox
                                            checked={condition.isActive}
                                            disabled={
                                                isPending || (!condition.isActive && activationIssue !== undefined)
                                            }
                                            onChange={(event: ChangeEvent<HTMLInputElement>) => {
                                                const isActive = event.target.checked;
                                                if (isActive && duplicateActiveCondition) {
                                                    setConditionIssue({
                                                        type: 'duplicate-target',
                                                        conditionIds: [condition.id, duplicateActiveCondition.id],
                                                    });
                                                    setError(DUPLICATE_TARGET_MESSAGE);
                                                    return;
                                                }
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
                                        Удалить правило
                                    </Button>
                                </div>
                            </div>
                            {!condition.isActive &&
                                (activationBlockReason ? (
                                    <p className={style.hint}>Правило нельзя активировать: {activationBlockReason}</p>
                                ) : (
                                    <p className={style.hint}>Активируйте правило, когда закончите изменения.</p>
                                ))}
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
                                        mutateAndRefresh={(mutation) => mutateTreeAndRefresh(condition, mutation)}
                                    />
                                </div>
                            ) : (
                                <AtomEditor
                                    page={page}
                                    disabled={isPending}
                                    onSubmit={(request) =>
                                        mutateTreeAndRefresh(condition, () => addConditionAtom(condition.id, request))
                                    }
                                />
                            )}
                        </article>
                    );
                })}
                {isCreating && (
                    <article className={style.condition}>
                        <div className={style.conditionHeader}>
                            <strong>Новое правило</strong>
                            <div className={style.targetField}>
                                <span>Перейти к</span>
                                <ConditionSelect
                                    name={`new-condition-target-${page.id}`}
                                    title='Страница перехода'
                                    value={newNextPageId}
                                    options={forwardPageOptions}
                                    disabled={pendingConditionIds.has('new')}
                                    onChange={setNewNextPageId}
                                />
                            </div>
                        </div>
                        <AtomEditor
                            page={page}
                            disabled={pendingConditionIds.has('new')}
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
                        Добавить правило перехода
                    </Button>
                )}
                {forwardPages.length === 0 && <p className={style.hint}>Это последняя страница опроса.</p>}
                {error && <p className={style.error}>{error}</p>}
            </div>
        </section>
    );
}
