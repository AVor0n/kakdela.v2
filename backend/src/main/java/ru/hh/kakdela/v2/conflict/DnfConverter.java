package ru.hh.kakdela.v2.conflict;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.hh.kakdela.v2.model.condition.Condition;
import ru.hh.kakdela.v2.model.condition.ConditionNode;
import ru.hh.kakdela.v2.model.condition.ConditionNode.Operator;

public class DnfConverter {

  private DnfConverter() {
  }

  public static DnfExpression convert(Condition condition) {
    Objects.requireNonNull(condition, "condition не может быть null");

    if (condition.getRoot() == null) {
      return DnfExpression.empty();
    }

    DnfExpression result = visitNode(condition.getRoot());

    return result.removeContradictions();
  }

  private static DnfExpression visitNode(ConditionNode node) {
    Objects.requireNonNull(node, "node не может быть null");

    Operator operator = node.getOperator();

    if (operator == Operator.ATOM) {
      Literal literal = Literal.ofPositive(node.getAtom());
      return DnfExpression.of(Clause.of(literal));
    }

    if (operator == Operator.NOT_ATOM) {
      Literal literal = Literal.ofNegative(node.getAtom());
      return DnfExpression.of(Clause.of(literal));
    }

    if (operator == Operator.AND) {
      return mergeAnd(node.getChildNodes());
    }

    if (operator == Operator.OR) {
      return mergeOr(node.getChildNodes());
    }

    throw new IllegalStateException("Неизвестный оператор: " + operator);
  }

  private static DnfExpression mergeAnd(List<ConditionNode> children) {
    if (children == null || children.isEmpty()) {
      return DnfExpression.empty();
    }

    List<DnfExpression> expressions = new ArrayList<>();
    for (ConditionNode child : children) {
      expressions.add(visitNode(child));
    }

    DnfExpression result = expressions.getFirst();
    for (int i = 1; i < expressions.size(); i++) {
      result = result.and(expressions.get(i));
    }

    return result;
  }

  private static DnfExpression mergeOr(List<ConditionNode> children) {
    if (children == null || children.isEmpty()) {
      return DnfExpression.empty();
    }

    DnfExpression result = DnfExpression.empty();
    for (ConditionNode child : children) {
      result = result.or(visitNode(child));
    }

    return result;
  }
}
