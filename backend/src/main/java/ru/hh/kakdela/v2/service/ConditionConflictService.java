package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.hh.kakdela.v2.conflict.ClauseAnalyzer;
import ru.hh.kakdela.v2.conflict.DnfConverter;
import ru.hh.kakdela.v2.conflict.DnfExpression;
import ru.hh.kakdela.v2.dao.ConditionDao;
import ru.hh.kakdela.v2.exception.ConditionConflictException;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.model.condition.Condition;
import ru.hh.kakdela.v2.model.condition.ConditionAtom;
import ru.hh.kakdela.v2.model.condition.ConditionNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionConflictService {

  private final ConditionDao conditionDao;

  public void validatePageConditions(UUID pageId) {
    List<Condition> conditions = conditionDao.findAllByPageId(pageId);

    if (conditions.size() < 2) {
      return;
    }

    List<DnfExpression> dnfs = conditions.stream()
        .map(DnfConverter::convert)
        .toList();

    for (int i = 0; i < dnfs.size(); i++) {
      for (int j = i + 1; j < dnfs.size(); j++) {
        Condition cond1 = conditions.get(i);
        Condition cond2 = conditions.get(j);

        if (ClauseAnalyzer.hasIntersection(dnfs.get(i), dnfs.get(j))) {
          String summary1 = buildConditionSummary(cond1);
          String summary2 = buildConditionSummary(cond2);

          log.warn("Конфликт: {} - {}", summary1, summary2);

          throw new ConditionConflictException(
              cond1.getId(),
              cond2.getId(),
              summary1,
              summary2,
              buildConflictDetails(cond1, cond2)
          );
        }
      }
    }

    log.debug("Конфликтов на странице {} не найдено", pageId);
  }

  private String buildConditionSummary(Condition condition) {
    if (condition.getRoot() == null) {
      return "Пустое условие " + getPageTitle(condition.getNextPage());
    }
    return "Если " + buildNodeSummary(condition.getRoot())
        + " - " + getPageTitle(condition.getNextPage());
  }

  private String buildNodeSummary(ConditionNode node) {
    if (node == null) {
      throw new IllegalStateException("Вершина условия не может быть null");
    }

    if (node.getOperator() == ConditionNode.Operator.ATOM
        || node.getOperator() == ConditionNode.Operator.NOT_ATOM) {
      return buildAtomSummary(node);
    }

    if (node.getOperator() == ConditionNode.Operator.AND) {
      return "(" + String.join(" И ", node.getChildNodes().stream()
          .map(this::buildNodeSummary)
          .toList()) + ")";
    }

    if (node.getOperator() == ConditionNode.Operator.OR) {
      return "(" + String.join(" ИЛИ ", node.getChildNodes().stream()
          .map(this::buildNodeSummary)
          .toList()) + ")";
    }

    throw new IllegalStateException(
        "Неизвестный оператор: " + node.getOperator());
  }

  private String buildAtomSummary(ConditionNode node) {
    ConditionAtom atom = node.getAtom();
    if (atom == null) {
      return "";
    }

    String questionText = atom.getQuestion().getText();
    if (questionText == null || questionText.isBlank()) {
      questionText = "Вопрос " + atom.getQuestion().getId().toString().substring(0, 8);
    }

    String operatorText = node.getOperator() == ConditionNode.Operator.NOT_ATOM ? "НЕ " : "";

    if (atom.getRequiredAnswerOption() != null) {
      String answerText = atom.getRequiredAnswerOption().getText();
      if (answerText == null || answerText.isBlank()) {
        answerText = "Вариант " + atom.getRequiredAnswerOption().getId().toString().substring(0, 8);
      }
      return String.format("%s %s= %s", questionText, operatorText, answerText);
    }

    if (atom.getRequiredBooleanValue() != null) {
      String value = atom.getRequiredBooleanValue() ? "Да" : "Нет";
      return String.format("%s %s= %s", questionText, operatorText, value);
    }

    return String.format("%s %s(значение не указано)", questionText, operatorText);
  }

  private String getPageTitle(SurveyPage page) {
    if (page == null) {
      return "неизвестная страница";
    }
    return page.getTitle() != null && !page.getTitle().isBlank()
        ? page.getTitle()
        : "Страница " + page.getSerialNumber();
  }

  private String buildConflictDetails(Condition cond1, Condition cond2) {
    return String.format(
            """
            Условия конфликтуют: существует набор ответов, при котором оба условия истинны.
            Они ведут на разные страницы: '%s' и '%s'.
            Рекомендуется проверить логику ветвления.
            """,
        getPageTitle(cond1.getNextPage()),
        getPageTitle(cond2.getNextPage())
    );
  }
}
