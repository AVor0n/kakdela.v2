export type ConditionLinkOperator = 'AND' | 'OR';
export type ConditionNodeOperator = ConditionLinkOperator | 'ATOM' | 'NOT_ATOM';
export type ConditionAtomOperator = 'EQUALS';

export type ConditionAtom = {
    questionId: string;
    requiredBooleanValue: boolean | null;
    requiredAnswerOptionId: string | null;
    operator: ConditionAtomOperator;
};

export type ConditionNode = {
    id: string;
    children: ConditionNode[];
    operator: ConditionNodeOperator;
    atom: ConditionAtom | null;
};

export type Condition = {
    id: string;
    pageId: string;
    nextPageId: string;
    isActive: boolean;
    root: ConditionNode | null;
};
