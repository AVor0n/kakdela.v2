import type { ConditionNode } from '@/shared/types/Condition.type';
import type { Page } from '@/shared/types/Survey.type';

export type ConditionAnswerValue = string | string[] | boolean;
export type ConditionAnswers = Record<string, ConditionAnswerValue>;

function someNode(node: ConditionNode | null, predicate: (_currentNode: ConditionNode) => boolean): boolean {
    if (!node) return false;
    return predicate(node) || node.children.some((child) => someNode(child, predicate));
}

export function isQuestionUsedInConditions(pages: Page[], questionId: string) {
    return pages.some((page) =>
        page.conditions.some((condition) => someNode(condition.root, (node) => node.atom?.questionId === questionId)),
    );
}

export function isAnswerOptionUsedInConditions(pages: Page[], answerOptionId: string) {
    return pages.some((page) =>
        page.conditions.some((condition) =>
            someNode(condition.root, (node) => node.atom?.requiredAnswerOptionId === answerOptionId),
        ),
    );
}

function evaluateAtom(node: ConditionNode, answers: ConditionAnswers): boolean {
    if (!node.atom) return false;

    const answer = answers[node.atom.questionId];
    if (answer === undefined) return false;

    let result = true;
    if (node.atom.requiredBooleanValue !== null) {
        result = typeof answer === 'boolean' && answer === node.atom.requiredBooleanValue;
    }
    if (node.atom.requiredAnswerOptionId !== null) {
        result =
            result &&
            (Array.isArray(answer)
                ? answer.includes(node.atom.requiredAnswerOptionId)
                : answer === node.atom.requiredAnswerOptionId);
    }
    return result;
}

export function evaluateConditionNode(node: ConditionNode | null, answers: ConditionAnswers): boolean {
    if (!node) return false;

    switch (node.operator) {
        case 'ATOM':
            return evaluateAtom(node, answers);
        case 'NOT_ATOM':
            return node.atom !== null && !evaluateAtom(node, answers);
        case 'AND':
            return node.children.every((child) => evaluateConditionNode(child, answers));
        case 'OR':
            return node.children.some((child) => evaluateConditionNode(child, answers));
    }
}

export function resolvePreviewNextPageId(currentPage: Page, pages: Page[], answers: ConditionAnswers): string | null {
    const activeConditions = currentPage.conditions.filter(({ isActive, root }) => isActive && root !== null);
    const matchedCondition = activeConditions.find((condition) => evaluateConditionNode(condition.root, answers));
    if (matchedCondition) return matchedCondition.nextPageId;

    const conditionTargetIds = new Set(activeConditions.map(({ nextPageId }) => nextPageId));
    const fallbackPage = [...pages]
        .sort((firstPage, secondPage) => firstPage.serialNumber - secondPage.serialNumber)
        .find((page) => page.serialNumber > currentPage.serialNumber && !conditionTargetIds.has(page.id));

    return fallbackPage?.id ?? null;
}
