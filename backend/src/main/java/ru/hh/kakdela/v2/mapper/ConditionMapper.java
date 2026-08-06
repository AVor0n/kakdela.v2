package ru.hh.kakdela.v2.mapper;

import ru.hh.kakdela.v2.dto.condition.ConditionResponseDto;
import ru.hh.kakdela.v2.dto.condition.atom.ConditionAtomResponseDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeResponseDto;
import ru.hh.kakdela.v2.model.condition.Condition;
import ru.hh.kakdela.v2.model.condition.ConditionAtom;
import ru.hh.kakdela.v2.model.condition.ConditionNode;

public class ConditionMapper {

  public static ConditionResponseDto conditionToDto(Condition condition) {
    return new ConditionResponseDto(
        condition.getId(),
        condition.getSurveyPage().getId(),
        condition.getNextPage().getId(),
        condition.getRoot() != null
            ? conditionNodeToDto(condition.getRoot())
            : null);
  }

  public static ConditionAtomResponseDto conditionAtomToDto(ConditionAtom conditionAtom) {
    return new ConditionAtomResponseDto(
        conditionAtom.getQuestion().getId(),
        conditionAtom.getRequiredBooleanValue(),
        conditionAtom.getRequiredAnswerOption() != null
            ? conditionAtom.getRequiredAnswerOption().getId()
            : null,
        conditionAtom.getOperator().name());
  }

  public static ConditionNodeResponseDto conditionNodeToDto(ConditionNode conditionNode) {
    if (conditionNode == null) {
      return null;
    }

    return new ConditionNodeResponseDto(
        conditionNode.getId(),
        conditionNode.getChildNodes().stream()
            .map(ConditionMapper::conditionNodeToDto)
            .toList(),
        conditionNode.getOperator().name(),
        conditionNode.getAtom() != null
            ? conditionAtomToDto(conditionNode.getAtom())
            : null);
  }
}
