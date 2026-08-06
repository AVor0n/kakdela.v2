package ru.hh.kakdela.v2.service;

import java.util.List;
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
  private final PermissionService permissionService;

  @Transactional
  public ConditionNodeResponseDto addNode(
      UUID conditionId,
      ConditionNodeCreateDto dto,
      UUID accountId
  ) {
    Condition condition = conditionDao.findById(conditionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Условие не найдено: id=" + conditionId));

    permissionService.checkCanEdit(
        condition.getSurveyPage().getSurvey().getId(), accountId);

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
            HttpStatus.NOT_FOUND, "Дочерняя вершина не найдена: id="
            + dto.getChildNodeToLinkId()));

    if (childNode.getHeight() == null) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Внутренняя ошибка. Пересоздайте дерево условия");
    }

    if (childNode.getParentNode() == null
        && childNode.getHeight() == MAX_CONDITION_TREE_HEIGHT
        || childNode.getParentNode() != null
        && childNode.getHeight() == 1) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Достигнута максимальная высота дерева условия");
    }

    int nodeHeight;

    if (childNode.getParentNode() == null) {
      nodeHeight = childNode.getHeight() + 1;
    } else {
      nodeHeight = childNode.getHeight();
      childNode.setHeight(childNode.getHeight() - 1);
    }

    ConditionNode node = ConditionNode.builder()
        .id(UUID.randomUUID())
        .condition(condition)
        .parentNode(childNode.getParentNode())
        .operator(dto.getOperator())
        .height(nodeHeight)
        .childNodes(List.of(childNode))
        .build();

    childNode.setParentNode(node);

    if (node.getParentNode() != null) {
      conditionNodeDao.save(node);
    } else {
      condition.setRoot(node);
      conditionDao.update(condition);
    }

    return ConditionMapper.conditionNodeToDto(node);
  }

  @Transactional
  public ConditionNodeResponseDto updateNode(
      UUID nodeId,
      ConditionNodeUpdateDto dto,
      UUID accountId
  ) {
    ConditionNode node = conditionNodeDao.findById(nodeId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вершина условия не найдена: id=" + nodeId));

    permissionService.checkCanEdit(
        node.getCondition().getSurveyPage().getSurvey().getId(), accountId);

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
    Condition condition = conditionDao.findById(conditionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Условие не найдено: id=" + conditionId));

    permissionService.checkCanEdit(
        condition.getSurveyPage().getSurvey().getId(), accountId);

    ConditionNode parentNode;
    int nodeHeight;

    if (condition.getRoot() != null) {
      if (dto.getParentNodeId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Дерево условия не пусто. Для новых атомарных условий"
                + " необходимо указывать родительскую вершину");
      }

      parentNode = conditionNodeDao.findById(dto.getParentNodeId())
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.NOT_FOUND, "Родительская вершина не найдена: id="
              + dto.getParentNodeId()));

      nodeHeight = parentNode.getHeight() - 1;

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
      nodeHeight = 1;
    }

    Question question = questionDao.findById(dto.getQuestionId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: id=" + dto.getQuestionId()));

    if (!question.getSurveyPage().getId().equals(condition.getSurveyPage().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Указанный вопрос принадлежит другой странице: id=" + dto.getQuestionId());
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
            "Указанный вариант ответа принадлежит другому вопросу: id=" + dto.getQuestionId());
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
        .height(nodeHeight)
        .build();

    ConditionAtom atom = ConditionAtom.builder()
        .node(node)
        .question(question)
        .requiredBooleanValue(dto.getRequiredBooleanValue())
        .requiredAnswerOption(requiredAnswerOption)
        .operator(dto.getOperator())
        .build();

    node.setAtom(atom);

    if (parentNode != null) {
      conditionNodeDao.save(node);
    } else {
      condition.setRoot(node);
      conditionDao.update(condition);
    }

    return ConditionMapper.conditionNodeToDto(node);
  }

  @Transactional
  public ConditionNodeResponseDto updateAtom(
      UUID nodeId,
      ConditionAtomUpdateDto dto,
      UUID accountId
  ) {
    ConditionNode node = conditionNodeDao.findById(nodeId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вершина условия не найдена: id=" + nodeId));

    permissionService.checkCanEdit(
        node.getCondition().getSurveyPage().getSurvey().getId(), accountId);


    Question question = questionDao.findById(dto.getQuestionId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: id=" + dto.getQuestionId()));

    if (!question.getSurveyPage().getId().equals(node.getCondition().getSurveyPage().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Указанный вопрос принадлежит другой странице: id=" + dto.getQuestionId());
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
            "Указанный вариант ответа принадлежит другому вопросу: id=" + dto.getQuestionId());
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
    ConditionNode node = conditionNodeDao.findById(nodeId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вершина условия не найдена: id=" + nodeId));

    permissionService.checkCanEdit(
        node.getCondition().getSurveyPage().getSurvey().getId(), accountId);

    if (node.getOperator().isLink && node.getChildNodes().size() > 1) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Удаление вершины возможно, только если она имеет максимум одну дочернюю вершину");
    }

    conditionNodeDao.delete(node);
  }

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
}
