import type { Condition, ConditionNode } from '@/shared/types/Condition.type';
import type { Question } from '@/shared/types/Question.type';
import type { Page } from '@/shared/types/Survey.type';

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

export * from './conditionEvaluation';
export * from './conditionReferences';
