import type { Page } from '@/shared/types/Survey.type';
import { someConditionNode } from './conditionTree';

export type PageConditionReference = {
    pageId: string;
    pageSerialNumber: number;
    conditionId: string;
    isActive: boolean;
};

export function findPageConditionReferences(pages: Page[], targetPageId: string): PageConditionReference[] {
    return pages.flatMap((page) =>
        page.conditions
            .filter(({ nextPageId }) => nextPageId === targetPageId)
            .map((condition) => ({
                pageId: page.id,
                pageSerialNumber: page.serialNumber,
                conditionId: condition.id,
                isActive: condition.isActive,
            })),
    );
}

export function isQuestionUsedInConditions(pages: Page[], questionId: string) {
    return pages.some((page) =>
        page.conditions.some((condition) =>
            someConditionNode(condition.root, (node) => node.atom?.questionId === questionId),
        ),
    );
}

export function isAnswerOptionUsedInConditions(pages: Page[], answerOptionId: string) {
    return pages.some((page) =>
        page.conditions.some((condition) =>
            someConditionNode(condition.root, (node) => node.atom?.requiredAnswerOptionId === answerOptionId),
        ),
    );
}
