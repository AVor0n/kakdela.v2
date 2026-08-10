import type { ConditionNode } from '@/shared/types/Condition.type';
import type { Page } from '@/shared/types/Survey.type';

function someNode(node: ConditionNode | null, predicate: (_currentNode: ConditionNode) => boolean): boolean {
    if (!node) return false;
    return predicate(node) || node.children.some((child) => someNode(child, predicate));
}

export function isQuestionUsedInConditions(pages: Page[], questionId: string) {
    return pages.some((page) =>
        page.conditions.some((condition) =>
            someNode(condition.root, (node) => node.atom?.questionId === questionId),
        ),
    );
}

export function isAnswerOptionUsedInConditions(pages: Page[], answerOptionId: string) {
    return pages.some((page) =>
        page.conditions.some((condition) =>
            someNode(condition.root, (node) => node.atom?.requiredAnswerOptionId === answerOptionId),
        ),
    );
}
