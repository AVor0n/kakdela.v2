import { apiClient } from '@/api/client';
import type {
    Condition,
    ConditionAtomOperator,
    ConditionLinkOperator,
    ConditionNode,
} from '@/shared/types/Condition.type';

export type ConditionRequest = {
    nextPageId: string;
    isActive: boolean;
};

export type VerifyPageResult = {
    nextPageId: string | null;
};

export type ConditionAtomRequest = {
    questionId: string;
    requiredBooleanValue?: boolean;
    requiredAnswerOptionId?: string;
    operator: ConditionAtomOperator;
    isNegative: boolean;
};

export type ConditionAtomCreateRequest = ConditionAtomRequest & {
    parentNodeId?: string;
};

export async function getPageConditions(pageId: string): Promise<Condition[]> {
    const { data } = await apiClient.get<Condition[]>(`/api/pages/${pageId}/conditions`);
    return data;
}

export async function getCondition(conditionId: string): Promise<Condition> {
    const { data } = await apiClient.get<Condition>(`/api/conditions/${conditionId}`);
    return data;
}

export async function createCondition(pageId: string, request: ConditionRequest): Promise<Condition> {
    const { data } = await apiClient.post<Condition>(`/api/pages/${pageId}/conditions`, request);
    return data;
}

export async function updateCondition(conditionId: string, request: ConditionRequest): Promise<Condition> {
    const { data } = await apiClient.put<Condition>(`/api/conditions/${conditionId}`, request);
    return data;
}

export async function deleteCondition(conditionId: string): Promise<void> {
    await apiClient.delete(`/api/conditions/${conditionId}`);
}

export async function addConditionNode(
    conditionId: string,
    childNodeToLinkId: string,
    operator: ConditionLinkOperator,
): Promise<ConditionNode> {
    const { data } = await apiClient.post<ConditionNode>(`/api/conditions/${conditionId}/nodes`, {
        childNodeToLinkId,
        operator,
    });
    return data;
}

export async function updateConditionNode(nodeId: string, operator: ConditionLinkOperator): Promise<ConditionNode> {
    const { data } = await apiClient.put<ConditionNode>(`/api/nodes/${nodeId}`, { operator });
    return data;
}

export async function addConditionAtom(
    conditionId: string,
    request: ConditionAtomCreateRequest,
): Promise<ConditionNode> {
    const { data } = await apiClient.post<ConditionNode>(`/api/conditions/${conditionId}/atoms`, request);
    return data;
}

export async function updateConditionAtom(nodeId: string, request: ConditionAtomRequest): Promise<ConditionNode> {
    const { data } = await apiClient.put<ConditionNode>(`/api/nodes/${nodeId}/atom`, request);
    return data;
}

export async function deleteConditionNode(nodeId: string): Promise<void> {
    await apiClient.delete(`/api/nodes/${nodeId}`);
}

export async function verifyPageConditions(pageId: string, responseId: string): Promise<VerifyPageResult> {
    const { data } = await apiClient.get<VerifyPageResult>(`/api/pages/${pageId}/verify`, {
        params: { responseId },
    });
    return data;
}
