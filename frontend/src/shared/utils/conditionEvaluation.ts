import type { ConditionNode } from '@/shared/types/Condition.type';
import type { Page } from '@/shared/types/Survey.type';

export type ConditionAnswerValue = string | string[] | boolean;
export type ConditionAnswers = Record<string, ConditionAnswerValue>;

function evaluateAtom(node: ConditionNode, answers: ConditionAnswers): boolean {
    if (!node.atom) return false;
    const answer = answers[node.atom.questionId];
    if (answer === undefined) return false;

    if (node.atom.requiredBooleanValue !== null) {
        return typeof answer === 'boolean' && answer === node.atom.requiredBooleanValue;
    }
    if (node.atom.requiredAnswerOptionId !== null) {
        return Array.isArray(answer)
            ? answer.includes(node.atom.requiredAnswerOptionId)
            : answer === node.atom.requiredAnswerOptionId;
    }
    return true;
}

export function evaluateConditionNode(node: ConditionNode | null, answers: ConditionAnswers): boolean {
    if (!node) return false;
    switch (node.operator) {
        case 'ATOM':
            return evaluateAtom(node, answers);
        case 'NOT_ATOM':
            return Boolean(node.atom) && !evaluateAtom(node, answers);
        case 'AND':
            return node.children.every((child) => evaluateConditionNode(child, answers));
        case 'OR':
            return node.children.some((child) => evaluateConditionNode(child, answers));
    }
}

export function resolvePreviewNextPageId(currentPage: Page, pages: Page[], answers: ConditionAnswers): string | null {
    const matchedCondition = currentPage.conditions
        .filter(({ isActive, root }) => isActive && root)
        .find((condition) => evaluateConditionNode(condition.root, answers));
    if (matchedCondition) return matchedCondition.nextPageId;

    return (
        [...pages]
            .sort((firstPage, secondPage) => firstPage.serialNumber - secondPage.serialNumber)
            .find((page) => page.serialNumber > currentPage.serialNumber)?.id ?? null
    );
}
