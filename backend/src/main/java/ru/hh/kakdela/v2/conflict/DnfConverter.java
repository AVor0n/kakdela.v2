package ru.hh.kakdela.v2.conflict;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.model.Clause;
import ru.hh.kakdela.v2.model.Literal;
import ru.hh.kakdela.v2.model.condition.Condition;
import ru.hh.kakdela.v2.model.condition.ConditionAtom;
import ru.hh.kakdela.v2.model.condition.ConditionNode;
import ru.hh.kakdela.v2.model.condition.ConditionNode.Operator;

@Component
@RequiredArgsConstructor
public class DnfConverter {

  public DnfExpression convert(Condition condition) {
    if (condition == null) {
      return DnfExpression.empty();
    }

    if (condition.getRoot() == null) {
      return DnfExpression.empty();
    }

    DnfExpression result = visitNode(condition.getRoot(), new HashSet<>());
    DnfExpression cleaned = result.removeContradictions();

    return cleaned;
  }

  private DnfExpression visitNode(ConditionNode node, Set<UUID> visited) {
    if (node == null) {
      return DnfExpression.empty();
    }

    if (visited.contains(node.getId())) {
      throw new IllegalStateException("Цикл в дереве условий: " + node.getId());
    }

    visited.add(node.getId());

    try {
      Operator operator = node.getOperator();

      if (operator == Operator.ATOM) {
        Literal literal = createLiteralFromNode(node, false);
        return DnfExpression.of(Clause.of(literal));
      }

      if (operator == Operator.NOT_ATOM) {
        Literal literal = createLiteralFromNode(node, true);
        return DnfExpression.of(Clause.of(literal));
      }

      if (operator == Operator.AND) {
        return mergeAnd(node.getChildNodes(), visited);
      }

      if (operator == Operator.OR) {
        return mergeOr(node.getChildNodes(), visited);
      }

      throw new IllegalStateException("Неизвестный оператор: " + operator);

    } finally {
      visited.remove(node.getId());
    }
  }

  private DnfExpression mergeAnd(List<ConditionNode> children, Set<UUID> visited) {
    if (children == null || children.isEmpty()) {
      return DnfExpression.empty();
    }

    List<DnfExpression> expressions = new ArrayList<>();
    for (ConditionNode child : children) {
      expressions.add(visitNode(child, visited));
    }

    DnfExpression result = expressions.get(0);
    for (int i = 1; i < expressions.size(); i++) {
      result = result.and(expressions.get(i));
    }

    return result;
  }

  private DnfExpression mergeOr(List<ConditionNode> children, Set<UUID> visited) {
    if (children == null || children.isEmpty()) {
      return DnfExpression.empty();
    }

    DnfExpression result = DnfExpression.empty();
    for (ConditionNode child : children) {
      result = result.or(visitNode(child, visited));
    }
    return result;
  }

  private Literal createLiteralFromNode(ConditionNode node, boolean isNegated) {
    ConditionAtom atom = node.getAtom();
    if (atom == null) {
      throw new IllegalStateException("ATOM узел не содержит атома: " + node.getId());
    }

    UUID questionId = atom.getQuestion().getId();

    if (atom.getRequiredAnswerOption() != null) {
      UUID optionId = atom.getRequiredAnswerOption().getId();
      return isNegated
          ? Literal.notEquals(questionId, optionId)
          : Literal.equals(questionId, optionId);
    } else {
      Boolean value = atom.getRequiredBooleanValue();
      if (value == null) {
        throw new IllegalStateException("Атом не содержит значения: " + node.getId());
      }
      return isNegated
          ? Literal.notEquals(questionId, value)
          : Literal.equals(questionId, value);
    }
  }
}
