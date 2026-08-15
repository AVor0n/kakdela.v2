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
import ru.hh.kakdela.v2.exception.condition.ConditionConflictException;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.model.condition.Condition;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionConflictService {

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

}
