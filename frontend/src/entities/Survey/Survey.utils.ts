import type { AnswerOption, Question } from '@/shared/types/Question.type';
import type { Page } from '@/shared/types/Survey.type';
import type { ConditionNode } from '@/shared/types/Condition.type';

export function cloneAnswerOption(answerOption: AnswerOption): AnswerOption {
    return { ...answerOption };
}

export function cloneQuestion(question: Question): Question {
    switch (question.type) {
        case 'SINGLE_CHOICE':
        case 'MULTIPLE_CHOICE':
            return {
                ...question,
                answerOptions: question.answerOptions.map(cloneAnswerOption),
            };
        case 'SHORT_TEXT':
        case 'LONG_TEXT':
        case 'YES_NO':
        case 'DATE':
        case 'TIME':
        default:
            return {
                ...question,
            };
    }
}

function cloneConditionNode(node: ConditionNode): ConditionNode {
    return {
        ...node,
        atom: node.atom ? { ...node.atom } : null,
        children: node.children.map(cloneConditionNode),
    };
}

export function clonePage(page: Page): Page {
    return {
        ...page,
        questions: page.questions.map(cloneQuestion),
        conditions: page.conditions.map((condition) => ({
            ...condition,
            root: condition.root ? cloneConditionNode(condition.root) : null,
        })),
    };
}
