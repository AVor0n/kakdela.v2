import type { ConditionNode } from '@/shared/types/Condition.type';

export function someConditionNode(node: ConditionNode | null, predicate: (_node: ConditionNode) => boolean): boolean {
    return Boolean(node && (predicate(node) || node.children.some((child) => someConditionNode(child, predicate))));
}
