import type { Condition, ConditionNode } from '@/shared/types/Condition.type';
import type { Question } from '@/shared/types/Question.type';
import type { Page } from '@/shared/types/Survey.type';

export type ConditionAnswerValue = string | string[] | boolean;
export type ConditionAnswers = Record<string, ConditionAnswerValue>;

export type ConditionValidationIssueCode =
    | 'MISSING_ROOT'
    | 'INVALID_ATOM'
    | 'INCOMPLETE_GROUP'
    | 'TARGET_PAGE_NOT_FOUND'
    | 'TARGET_PAGE_NOT_FORWARD';

export type ConditionValidationIssue = {
    code: ConditionValidationIssueCode;
    pageId: string;
    pageSerialNumber: number;
    conditionId: string;
    nodeId?: string;
    targetPageId?: string;
};

export type PageConditionReference = {
    pageId: string;
    pageSerialNumber: number;
    conditionId: string;
    isActive: boolean;
};

function hasConditionAtom(node: ConditionNode): boolean {
    return node.atom !== null && node.atom !== undefined;
}

function isConditionAtomComplete(node: ConditionNode, questions: Question[]): boolean {
    if (!node.atom || node.children.length > 0 || node.atom.operator !== 'EQUALS') return false;

    const question = questions.find(({ id }) => id === node.atom?.questionId);
    if (!question) return false;

    if (question.type === 'YES_NO') {
        return node.atom.requiredBooleanValue !== null && node.atom.requiredAnswerOptionId === null;
    }

    if (question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') {
        return (
            node.atom.requiredBooleanValue === null &&
            node.atom.requiredAnswerOptionId !== null &&
            question.answerOptions.some(({ id }) => id === node.atom?.requiredAnswerOptionId)
        );
    }

    return false;
}

export function isConditionTreeComplete(node: ConditionNode | null, questions: Question[]): boolean {
    if (!node) return false;

    if (node.operator === 'ATOM' || node.operator === 'NOT_ATOM') {
        return isConditionAtomComplete(node, questions);
    }

    return (
        !hasConditionAtom(node) &&
        node.children.length >= 2 &&
        node.children.every((child) => isConditionTreeComplete(child, questions))
    );
}

function collectTreeValidationIssues(node: ConditionNode, page: Page, conditionId: string): ConditionValidationIssue[] {
    if (node.operator === 'ATOM' || node.operator === 'NOT_ATOM') {
        return isConditionAtomComplete(node, page.questions)
            ? []
            : [
                  {
                      code: 'INVALID_ATOM',
                      pageId: page.id,
                      pageSerialNumber: page.serialNumber,
                      conditionId,
                      nodeId: node.id,
                  },
              ];
    }

    const issues: ConditionValidationIssue[] = [];
    if (hasConditionAtom(node) || node.children.length < 2) {
        issues.push({
            code: 'INCOMPLETE_GROUP',
            pageId: page.id,
            pageSerialNumber: page.serialNumber,
            conditionId,
            nodeId: node.id,
        });
    }

    node.children.forEach((child) => issues.push(...collectTreeValidationIssues(child, page, conditionId)));
    return issues;
}

export function validateCondition(condition: Condition, page: Page, pages: Page[]): ConditionValidationIssue[] {
    const issues: ConditionValidationIssue[] = [];

    if (!condition.root) {
        issues.push({
            code: 'MISSING_ROOT',
            pageId: page.id,
            pageSerialNumber: page.serialNumber,
            conditionId: condition.id,
        });
    } else {
        issues.push(...collectTreeValidationIssues(condition.root, page, condition.id));
    }

    const targetPage = pages.find(({ id }) => id === condition.nextPageId);
    if (!targetPage) {
        issues.push({
            code: 'TARGET_PAGE_NOT_FOUND',
            pageId: page.id,
            pageSerialNumber: page.serialNumber,
            conditionId: condition.id,
            targetPageId: condition.nextPageId,
        });
    } else if (targetPage.serialNumber <= page.serialNumber) {
        issues.push({
            code: 'TARGET_PAGE_NOT_FORWARD',
            pageId: page.id,
            pageSerialNumber: page.serialNumber,
            conditionId: condition.id,
            targetPageId: condition.nextPageId,
        });
    }

    return issues;
}

export function validateActiveSurveyConditions(pages: Page[]): ConditionValidationIssue[] {
    return pages.flatMap((page) =>
        page.conditions
            .filter(({ isActive }) => isActive)
            .flatMap((condition) => validateCondition(condition, page, pages)),
    );
}

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
            return hasConditionAtom(node) && !evaluateAtom(node, answers);
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

    const fallbackPage = [...pages]
        .sort((firstPage, secondPage) => firstPage.serialNumber - secondPage.serialNumber)
        .find((page) => page.serialNumber > currentPage.serialNumber);

    return fallbackPage?.id ?? null;
}
