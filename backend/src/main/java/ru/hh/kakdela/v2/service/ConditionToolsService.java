package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.hh.kakdela.v2.conflict.ClauseAnalyzer;
import ru.hh.kakdela.v2.conflict.DnfConverter;
import ru.hh.kakdela.v2.conflict.DnfExpression;
import ru.hh.kakdela.v2.dao.ConditionDao;
import ru.hh.kakdela.v2.exception.condition.ConditionConflictException;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.model.condition.Condition;
import ru.hh.kakdela.v2.model.condition.ConditionAtom;
import ru.hh.kakdela.v2.model.condition.ConditionNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionToolsService {

  private final ConditionDao conditionDao;

  public void validatePageConditions(SurveyPage page) {
    List<Condition> activeConditions = page.getConditions().stream()
        .filter(Condition::getIsActive)
        .toList();

    if (activeConditions.size() < 2) {
      return;
    }

    List<DnfExpression> dnfs = activeConditions.stream()
        .map(DnfConverter::convert)
        .toList();
    log.debug(dnfs.toString());

    for (int i = 0; i < dnfs.size(); i++) {
      for (int j = i + 1; j < dnfs.size(); j++) {
        if (ClauseAnalyzer.hasIntersection(dnfs.get(i), dnfs.get(j))) {
          UUID id1 = activeConditions.get(i).getId();
          UUID id2 = activeConditions.get(j).getId();

          log.warn("Конфликт: {} — {}", id1, id2);
          throw new ConditionConflictException(id1, id2);
        }
      }
    }

    log.debug("Конфликтов на странице {} не найдено", page.getId());
  }

  void makeConditionsConsistent(UUID pageId, int serialNumber) {
    conditionDao.makeConditionsConsistentByPageIdAndItsSerialNumber(pageId, serialNumber);
  }

  boolean doSurveyHaveConditions(UUID surveyId) {
    return conditionDao.existsBySurveyId(surveyId);
  }

  ConditionAndRoot cloneCondition(Condition originalCondition, Map<Integer, SurveyPage> pageMap) {
    int originalSurveyPageSerialNumber = originalCondition.getSurveyPage().getSerialNumber();
    SurveyPage surveyPageCopy = pageMap.get(originalSurveyPageSerialNumber);

    int originalNextPageSerialNumber = originalCondition.getNextPage().getSerialNumber();
    SurveyPage nextPageCopy = pageMap.get(originalNextPageSerialNumber);

    Condition conditionCopy = Condition.builder()
        .id(UUID.randomUUID())
        .surveyPage(surveyPageCopy)
        .nextPage(nextPageCopy)
        .isActive(originalCondition.getIsActive())
        .build();

    ConditionNode root;
    if (originalCondition.getRoot() != null) {
      root = cloneConditionNode(originalCondition.getRoot(), null, conditionCopy);
    } else {
      root = null;
    }

    return new ConditionAndRoot(conditionCopy, root);
  }

  private ConditionNode cloneConditionNode(
      ConditionNode originalNode,
      ConditionNode parentNodeCopy,
      Condition conditionCopy
  ) {
    ConditionNode nodeCopy = ConditionNode.builder()
        .id(UUID.randomUUID())
        .condition(conditionCopy)
        .parentNode(parentNodeCopy)
        .operator(originalNode.getOperator())
        .build();

    if (originalNode.getAtom() != null) {
      nodeCopy.setAtom(cloneConditionAtom(
          originalNode.getAtom(), nodeCopy, conditionCopy));
    }

    for (ConditionNode childNode : originalNode.getChildNodes()) {
      nodeCopy.getChildNodes().add(cloneConditionNode(
          childNode, nodeCopy, conditionCopy));
    }

    return nodeCopy;
  }

  private ConditionAtom cloneConditionAtom(
      ConditionAtom originalAtom,
      ConditionNode nodeCopy,
      Condition conditionCopy) {

    int originalQuestionSerialNumber = originalAtom.getQuestion().getSerialNumber();
    Question questionCopy = conditionCopy.getSurveyPage().getQuestions().stream()
        .filter(q -> q.getSerialNumber() == originalQuestionSerialNumber)
        .findAny().orElseThrow();

    AnswerOption optionCopy;

    if (originalAtom.getRequiredAnswerOption() != null) {
      int originalOptionSerialNumber = originalAtom.getRequiredAnswerOption().getSerialNumber();
      optionCopy = questionCopy.getAnswerOptions().stream()
          .filter(ao -> ao.getSerialNumber() == originalOptionSerialNumber)
          .findAny().orElseThrow();
    } else {
      optionCopy = null;
    }

    return ConditionAtom.builder()
        .node(nodeCopy)
        .question(questionCopy)
        .operator(originalAtom.getOperator())
        .requiredBooleanValue(originalAtom.getRequiredBooleanValue())
        .requiredAnswerOption(optionCopy)
        .build();
  }

  record ConditionAndRoot(Condition condition, ConditionNode root) {}
}
