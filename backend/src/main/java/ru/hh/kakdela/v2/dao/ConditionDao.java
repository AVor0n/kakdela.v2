package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.condition.Condition;

public interface ConditionDao {

  Optional<Condition> findById(UUID id);

  Optional<Condition> findByIdWithWholeTree(UUID id);

  UUID findParentSurveyIdById(UUID id);

  List<Condition> findAllByPageId(UUID pageId);

  boolean existsByPageIdAndNextPageId(UUID pageId, UUID nextPageId);

  boolean existsBySurveyId(UUID surveyId);

  void makeConditionsConsistentByPageIdAndItsSerialNumber(UUID pageId, int serialNumber);

  void save(Condition condition);

  void update(Condition condition);

  void delete(Condition condition);
}
