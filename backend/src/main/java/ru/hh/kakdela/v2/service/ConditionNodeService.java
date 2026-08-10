package ru.hh.kakdela.v2.service;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AnswerOptionDao;
import ru.hh.kakdela.v2.dao.ConditionDao;
import ru.hh.kakdela.v2.dao.ConditionNodeDao;
import ru.hh.kakdela.v2.dao.QuestionDao;
import ru.hh.kakdela.v2.dto.condition.atom.ConditionAtomCreateDto;
import ru.hh.kakdela.v2.dto.condition.atom.ConditionAtomUpdateDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeCreateDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeResponseDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeUpdateDto;
import ru.hh.kakdela.v2.mapper.ConditionMapper;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.condition.Condition;
import ru.hh.kakdela.v2.model.condition.ConditionAtom;
import ru.hh.kakdela.v2.model.condition.ConditionNode;

@Service
@RequiredArgsConstructor
public class ConditionNodeService {

  private static final int MAX_CONDITION_TREE_HEIGHT = 3;

  private final ConditionDao conditionDao;
  private final ConditionNodeDao conditionNodeDao;
  private final QuestionDao questionDao;
  private final AnswerOptionDao answerOptionDao;
  private final ConditionService conditionService;
  private final PermissionService permissionService;

  @Transactional
  public ConditionNodeResponseDto addNode(
      UUID conditionId,
      ConditionNodeCreateDto dto,
      UUID accountId
  ) {
    Condition condition = conditionService.getFullyInitializedEntityById(conditionId);

    permissionService.checkCanEdit(
        conditionService.getParentSurveyId(conditionId), accountId);

    if (!dto.getOperator().isLink) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Указанный оператор вершины не является соединительным");
    }

    if (condition.getRoot() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Дерево условия пусто. Добавление соединительных вершин не допускается");
    }

    ConditionNode childNode = conditionNodeDao.findById(dto.getChildNodeToLinkId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Дочерняя вершина не найдена: id=" + dto.getChildNodeToLinkId()));

    if (!childNode.getCondition().getId().equals(conditionId)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Дочерняя вершина не найдена: id=" + dto.getChildNodeToLinkId());
    }

    ConditionNode node = ConditionNode.builder()
        .id(UUID.randomUUID())
        .condition(condition)
        .parentNode(childNode.getParentNode())
        .operator(dto.getOperator())
        .build();

    if (node.getParentNode() == null) {
      condition.setRoot(node);
    } else {
      childNode.getParentNode().getChildNodes().remove(childNode);
      childNode.getParentNode().getChildNodes().add(node);
    }

    childNode.setParentNode(node);
    node.getChildNodes().add(childNode);

    checkConditionTreeHeight(condition);

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
    ConditionNode node = getEntityById(nodeId);

    permissionService.checkCanEdit(
        conditionNodeDao.findParentSurveyIdById(nodeId), accountId);

    if (!node.getOperator().isLink) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Изменять можно только соединительные вершины");
    }

    node.setOperator(dto.getOperator());

    conditionNodeDao.update(node);

    return ConditionMapper.conditionNodeToDto(node);
  }

  @Transactional
  public ConditionNodeResponseDto addAtom(
      UUID conditionId,
      ConditionAtomCreateDto dto,
      UUID accountId
  ) {
    Condition condition = conditionService.getFullyInitializedEntityById(conditionId);

    permissionService.checkCanEdit(
        conditionService.getParentSurveyId(conditionId), accountId);

    ConditionNode parentNode;

    if (condition.getRoot() != null) {
      if (dto.getParentNodeId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Дерево условия не пусто. Для новых атомарных условий "
                + "необходимо указывать родительскую вершину");
      }

      parentNode = conditionNodeDao.findById(dto.getParentNodeId())
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.NOT_FOUND, "Родительская вершина не найдена: id="
              + dto.getParentNodeId()));

      if (!parentNode.getCondition().getId().equals(conditionId)) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Родительская вершина не найдена: id=" + dto.getParentNodeId());
      }

      if (!parentNode.getOperator().isLink) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Указанная родительская вершина не является соединительной");
      }
    } else  {
      if (dto.getParentNodeId() != null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Дерево условия пусто. У нового атомарного условия"
                + " не может быть родительской вершины");
      }

      parentNode = null;
    }

    Question question = questionDao.findById(dto.getQuestionId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: id=" + dto.getQuestionId()));

    if (!question.getSurveyPage().getId().equals(condition.getSurveyPage().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Вопрос не найден: id=" + dto.getQuestionId());
    }

    verifyAtomRequestDto(dto, question);

    AnswerOption requiredAnswerOption;
    if (dto.getRequiredAnswerOptionId() != null) {
      requiredAnswerOption = answerOptionDao.findById(dto.getRequiredAnswerOptionId())
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.NOT_FOUND, "Вариант ответа не найден: id="
                  + dto.getRequiredAnswerOptionId()));

      if (!requiredAnswerOption.getQuestion().getId().equals(question.getId())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Вариант ответа не найден: id=" + dto.getQuestionId());
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
    ConditionNode node = getEntityById(nodeId);

    permissionService.checkCanEdit(
        conditionNodeDao.findParentSurveyIdById(nodeId), accountId);

    Question question = questionDao.findById(dto.getQuestionId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: id=" + dto.getQuestionId()));

    if (!question.getSurveyPage().getId().equals(node.getCondition().getSurveyPage().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Вопрос не найден: id=" + dto.getQuestionId());
    }

    verifyAtomRequestDto(dto, question);

    AnswerOption requiredAnswerOption;
    if (dto.getRequiredAnswerOptionId() != null) {
      requiredAnswerOption = answerOptionDao.findById(dto.getRequiredAnswerOptionId())
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.NOT_FOUND, "Вариант ответа не найден: id="
              + dto.getRequiredAnswerOptionId()));

      if (!requiredAnswerOption.getQuestion().getId().equals(question.getId())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Вариант ответа не найден: id=" + dto.getQuestionId());
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

    conditionNodeDao.update(node);

    return ConditionMapper.conditionNodeToDto(node);
  }

  @Transactional
  public void delete(UUID nodeId, UUID accountId) {
    ConditionNode node = getEntityWithParentNodeAndParentConditionById(nodeId);

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
    }

    conditionNodeDao.delete(nodeToDelete);
  }

  // Вспомогательные методы

  private void verifyAtomRequestDto(ConditionAtomUpdateDto dto, Question question) {
    final Question.QuestionType questionType = question.getType();

    if (!dto.getOperator().allowedQuestionTypes.contains(question.getType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Оператор %s несовместим с вопросом типа %s"
              .formatted(dto.getOperator(), question.getType()));
    }

    if (questionType.isBooleanAllowed) {
      if (dto.getRequiredBooleanValue() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Для вопроса типа %s в атомарном условии должно быть указано булевое значение"
                .formatted(questionType));
      }
    } else {
      if (dto.getRequiredBooleanValue() != null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Для вопроса типа %s в атомарном условии не должно быть указано булевое значение"
                .formatted(questionType));
      }
    }

    if (questionType.isAnswerOptionsAllowed) {
      if (dto.getRequiredAnswerOptionId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Для вопроса типа %s в атомарном условии должен быть указан вариант ответа"
                .formatted(questionType));
      }
    } else {
      if (dto.getRequiredAnswerOptionId() != null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Для вопроса типа %s в атомарном условии не должен быть указан вариант ответа"
                .formatted(questionType));
      }
    }
  }

  private void checkConditionTreeHeight(Condition condition) {
    record NodeWithHeight(ConditionNode node, int height) {}

    Queue<NodeWithHeight> queue = new ArrayDeque<>();

    for (ConditionNode child : condition.getRoot().getChildNodes()) {
      queue.add(new NodeWithHeight(child, 2));
    }

    while (!queue.isEmpty()) {
      NodeWithHeight current = queue.poll();

      if (current.height() > MAX_CONDITION_TREE_HEIGHT) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Превышена максимальная высота дерева условия");
      }

      for (ConditionNode child : current.node().getChildNodes()) {
        queue.add(new NodeWithHeight(child, current.height() + 1));
      }
    }
  }

  ConditionNode getEntityById(UUID id) {
    return conditionNodeDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вершина условия не найдена: id=" + id));
  }

  ConditionNode getEntityWithParentNodeAndParentConditionById(UUID id) {
    return conditionNodeDao.findByIdWithParentAndGrandparentNodeAndParentCondition(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вершина условия не найдена: id=" + id));
  }
}
