package ru.hh.kakdela.v2.service;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.kakdela.v2.dao.ConditionDao;
import ru.hh.kakdela.v2.dao.ConditionNodeDao;
import ru.hh.kakdela.v2.dto.condition.atom.ConditionAtomCreateDto;
import ru.hh.kakdela.v2.dto.condition.atom.ConditionAtomUpdateDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeCreateDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeResponseDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeUpdateDto;
import ru.hh.kakdela.v2.exception.BadRequestDataException;
import ru.hh.kakdela.v2.exception.condition.ConditionChildNodeNotFoundException;
import ru.hh.kakdela.v2.exception.condition.ConditionLinkNodesOnlyShouldBeModifiedException;
import ru.hh.kakdela.v2.exception.condition.ConditionNodeIsNotAtomException;
import ru.hh.kakdela.v2.exception.condition.ConditionNodeNotFoundException;
import ru.hh.kakdela.v2.exception.condition.ConditionNodeOperatorIsNotLinkException;
import ru.hh.kakdela.v2.exception.condition.ConditionParentNodeNotFoundException;
import ru.hh.kakdela.v2.exception.condition.ConditionTreeHeightLimitReachedException;
import ru.hh.kakdela.v2.exception.condition.ConditionTreeIsEmptyException;
import ru.hh.kakdela.v2.exception.condition.ConditionTreeIsNotEmptyException;
import ru.hh.kakdela.v2.exception.question.AnswerOptionNotFoundException;
import ru.hh.kakdela.v2.exception.question.QuestionNotFoundException;
import ru.hh.kakdela.v2.mapper.ConditionMapper;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.condition.Condition;
import ru.hh.kakdela.v2.model.condition.ConditionAtom;
import ru.hh.kakdela.v2.model.condition.ConditionNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionNodeService {

  private static final int MAX_CONDITION_TREE_HEIGHT = 3;

  private final ConditionService conditionService;
  private final ConditionToolsService conditionToolsService;
  private final ConditionDao conditionDao;
  private final ConditionNodeDao conditionNodeDao;
  private final QuestionService questionService;
  private final AnswerOptionService answerOptionService;
  private final PermissionService permissionService;

  @Transactional
  public ConditionNodeResponseDto addNode(
      UUID conditionId,
      ConditionNodeCreateDto dto,
      UUID accountId
  ) {
    log.info("Начато добавление вершины к дереву условия: conditionId={}", conditionId);

    Condition condition =
        conditionService.getEntityWithParentPageWithAllQuestionsAndNeighbourConditionsById(
            conditionId);

    permissionService.checkCanEdit(
        conditionService.getParentSurveyId(conditionId), accountId);

    if (!dto.getOperator().isLink) {
      throw new ConditionNodeOperatorIsNotLinkException();
    }

    if (condition.getRoot() == null) {
      throw new ConditionTreeIsEmptyException();
    }

    ConditionNode childNode = getOptionalById(dto.getChildNodeToLinkId())
        .orElseThrow(() -> new ConditionChildNodeNotFoundException(dto.getChildNodeToLinkId()));
    ConditionNode parentNode = childNode.getParentNode();

    if (!childNode.getCondition().getId().equals(conditionId)) {
      throw new ConditionChildNodeNotFoundException(dto.getChildNodeToLinkId());
    }

    ConditionNode node = ConditionNode.builder()
        .id(UUID.randomUUID())
        .condition(condition)
        .parentNode(childNode.getParentNode())
        .operator(dto.getOperator())
        .build();

    if (parentNode == null) {
      condition.setRoot(node);
    } else {
      parentNode.getChildNodes().remove(childNode);
      parentNode.getChildNodes().add(node);
    }

    childNode.setParentNode(node);
    node.getChildNodes().add(childNode);

    checkConditionTreeHeight(condition);

    if (condition.getIsActive()) {
      conditionToolsService.validatePageConditions(condition.getSurveyPage());
    }

    if (node.getParentNode() == null) {
      conditionDao.update(condition);
    } else {
      conditionNodeDao.save(node);
    }

    return ConditionMapper.conditionNodeToDto(node);
  }

  @Transactional
  public ConditionNodeResponseDto updateNode(
      UUID nodeId,
      ConditionNodeUpdateDto dto,
      UUID accountId
  ) {
    log.info("Начато изменение вершины дерева условия: id={}", nodeId);

    ConditionNode node =
        getEntityWithParentConditionAndParentPageWithAllQuestionsAndNeighbourConditionsById(nodeId);

    permissionService.checkCanEdit(
        conditionNodeDao.findParentSurveyIdById(nodeId), accountId);

    if (!node.getOperator().isLink) {
      throw new ConditionLinkNodesOnlyShouldBeModifiedException();
    }

    node.setOperator(dto.getOperator());

    if (node.getCondition().getIsActive()) {
      conditionToolsService.validatePageConditions(node.getCondition().getSurveyPage());
    }

    conditionNodeDao.update(node);

    return ConditionMapper.conditionNodeToDto(node);
  }

  @Transactional
  public ConditionNodeResponseDto addAtom(
      UUID conditionId,
      ConditionAtomCreateDto dto,
      UUID accountId
  ) {
    log.info("Начато добавление листа к дереву условия: conditionId={}", conditionId);

    Condition condition =
        conditionService.getEntityWithParentPageWithAllQuestionsAndNeighbourConditionsById(
            conditionId);

    permissionService.checkCanEdit(
        conditionService.getParentSurveyId(conditionId), accountId);

    ConditionNode parentNode;

    if (condition.getRoot() != null) {
      if (dto.getParentNodeId() == null) {
        throw new ConditionTreeIsNotEmptyException();
      }

      parentNode = getOptionalById(dto.getParentNodeId())
          .orElseThrow(() -> new ConditionParentNodeNotFoundException(dto.getParentNodeId()));

      if (!parentNode.getCondition().getId().equals(conditionId)) {
        throw new ConditionParentNodeNotFoundException(dto.getParentNodeId());
      }

      if (!parentNode.getOperator().isLink) {
        throw new ConditionNodeOperatorIsNotLinkException();
      }
    } else {
      if (dto.getParentNodeId() != null) {
        throw new ConditionTreeIsEmptyException();
      }

      parentNode = null;
    }

    Question question = questionService.getEntityById(dto.getQuestionId());

    if (!question.getSurveyPage().getId().equals(condition.getSurveyPage().getId())) {
      throw new QuestionNotFoundException(dto.getQuestionId());
    }

    verifyAtomRequestDto(dto, question);

    AnswerOption requiredAnswerOption;
    if (dto.getRequiredAnswerOptionId() != null) {
      requiredAnswerOption = answerOptionService.getEntityById(dto.getRequiredAnswerOptionId());

      if (!requiredAnswerOption.getQuestion().getId().equals(question.getId())) {
        throw new AnswerOptionNotFoundException(dto.getRequiredAnswerOptionId());
      }
    } else {
      requiredAnswerOption = null;
    }

    ConditionNode node = ConditionNode.builder()
        .id(UUID.randomUUID())
        .condition(condition)
        .parentNode(parentNode)
        .operator(dto.getIsNegative()
            ? ConditionNode.Operator.NOT_ATOM
            : ConditionNode.Operator.ATOM)
        .build();

    ConditionAtom atom = ConditionAtom.builder()
        .node(node)
        .question(question)
        .requiredBooleanValue(dto.getRequiredBooleanValue())
        .requiredAnswerOption(requiredAnswerOption)
        .operator(dto.getOperator())
        .build();

    node.setAtom(atom);

    if (condition.getIsActive()) {
      conditionToolsService.validatePageConditions(condition.getSurveyPage());
    }

    if (parentNode == null) {
      condition.setRoot(node);
      conditionDao.update(condition);
    } else {
      conditionNodeDao.save(node);
    }

    return ConditionMapper.conditionNodeToDto(node);
  }

  @Transactional
  public ConditionNodeResponseDto updateAtom(
      UUID nodeId,
      ConditionAtomUpdateDto dto,
      UUID accountId
  ) {
    log.info("Начато изменение листа дерева условия: id={}", nodeId);

    ConditionNode node =
        getEntityWithParentConditionAndParentPageWithAllQuestionsAndNeighbourConditionsById(nodeId);

    permissionService.checkCanEdit(
        conditionNodeDao.findParentSurveyIdById(nodeId), accountId);

    if (node.getAtom() == null) {
      throw new ConditionNodeIsNotAtomException(nodeId);
    }

    Question question = questionService.getEntityById(dto.getQuestionId());

    if (!question.getSurveyPage().getId().equals(node.getCondition().getSurveyPage().getId())) {
      throw new QuestionNotFoundException(dto.getQuestionId());
    }

    verifyAtomRequestDto(dto, question);

    AnswerOption requiredAnswerOption;
    if (dto.getRequiredAnswerOptionId() != null) {
      requiredAnswerOption = answerOptionService.getEntityById(dto.getRequiredAnswerOptionId());

      if (!requiredAnswerOption.getQuestion().getId().equals(question.getId())) {
        throw new AnswerOptionNotFoundException(dto.getRequiredAnswerOptionId());
      }
    } else {
      requiredAnswerOption = null;
    }

    node.setOperator(dto.getIsNegative()
        ? ConditionNode.Operator.NOT_ATOM
        : ConditionNode.Operator.ATOM);
    node.getAtom().setQuestion(question);
    node.getAtom().setRequiredBooleanValue(dto.getRequiredBooleanValue());
    node.getAtom().setRequiredAnswerOption(requiredAnswerOption);
    node.getAtom().setOperator(dto.getOperator());

    if (node.getCondition().getIsActive()) {
      conditionToolsService.validatePageConditions(node.getCondition().getSurveyPage());
    }

    conditionNodeDao.update(node);

    return ConditionMapper.conditionNodeToDto(node);
  }

  @Transactional
  public void delete(UUID nodeId, UUID accountId) {
    log.info("Начато удаление вершины дерева условия: id={}", nodeId);

    ConditionNode node =
        getEntityWithParentConditionAndParentPageWithAllQuestionsAndNeighbourConditionsById(nodeId);

    permissionService.checkCanEdit(
        conditionNodeDao.findParentSurveyIdById(nodeId), accountId);

    ConditionNode nodeToDelete;

    if (node.getParentNode() != null
        && conditionNodeDao.doesNodeHaveOneChild(node.getParentNode().getId())) {
      nodeToDelete = node.getParentNode();
    } else {
      nodeToDelete = node;
    }

    if (nodeToDelete.getParentNode() == null) {
      nodeToDelete.getCondition().setRoot(null);
    } else {
      nodeToDelete.getParentNode().getChildNodes().remove(nodeToDelete);
    }

    if (node.getCondition().getIsActive()) {
      conditionToolsService.validatePageConditions(node.getCondition().getSurveyPage());
    }

    conditionNodeDao.delete(nodeToDelete);
  }

  // Вспомогательные методы

  private void verifyAtomRequestDto(ConditionAtomUpdateDto dto, Question question) {
    final Question.QuestionType questionType = question.getType();

    if (!dto.getOperator().allowedQuestionTypes.contains(question.getType())) {
      throw new BadRequestDataException(
          "Оператор %s несовместим с вопросом типа %s"
              .formatted(dto.getOperator(), question.getType()));
    }

    if (questionType.isBooleanAllowed) {
      if (dto.getRequiredBooleanValue() == null) {
        throw new BadRequestDataException(
            "Для вопроса типа %s в атомарном условии должно быть указано булевое значение"
                .formatted(questionType));
      }
    } else {
      if (dto.getRequiredBooleanValue() != null) {
        throw new BadRequestDataException(
            "Для вопроса типа %s в атомарном условии не должно быть указано булевое значение"
                .formatted(questionType));
      }
    }

    if (questionType.isAnswerOptionsAllowed) {
      if (dto.getRequiredAnswerOptionId() == null) {
        throw new BadRequestDataException(
            "Для вопроса типа %s в атомарном условии должен быть указан вариант ответа"
                .formatted(questionType));
      }
    } else {
      if (dto.getRequiredAnswerOptionId() != null) {
        throw new BadRequestDataException(
            "Для вопроса типа %s в атомарном условии не должен быть указан вариант ответа"
                .formatted(questionType));
      }
    }
  }

  private void checkConditionTreeHeight(Condition condition) {
    record NodeWithHeight(ConditionNode node, int height) {
    }

    Queue<NodeWithHeight> queue = new ArrayDeque<>();

    for (ConditionNode child : condition.getRoot().getChildNodes()) {
      queue.add(new NodeWithHeight(child, 2));
    }

    while (!queue.isEmpty()) {
      NodeWithHeight current = queue.poll();

      if (current.height() > MAX_CONDITION_TREE_HEIGHT) {
        throw new ConditionTreeHeightLimitReachedException();
      }

      for (ConditionNode child : current.node().getChildNodes()) {
        queue.add(new NodeWithHeight(child, current.height() + 1));
      }
    }
  }

  ConditionNode
      getEntityWithParentConditionAndParentPageWithAllQuestionsAndNeighbourConditionsById(UUID id) {
    return conditionNodeDao
        .findByIdWithParentConditionAndParentPageWithAllQuestionsAndNeighbourConditions(id)
        .orElseThrow(() -> new ConditionNodeNotFoundException(id));
  }

  Optional<ConditionNode> getOptionalById(UUID id) {
    return conditionNodeDao.findById(id);
  }
}
