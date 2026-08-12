package ru.hh.kakdela.v2.dao;

import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.condition.ConditionNode;

public interface ConditionNodeDao {

  Optional<ConditionNode> findById(UUID id);

  Optional<ConditionNode>
      findByIdWithParentConditionAndParentPageWithAllQuestionsAndNeighbourConditions(UUID id);

  UUID findParentSurveyIdById(UUID id);

  boolean doesNodeHaveOneChild(UUID id);

  void save(ConditionNode conditionNode);

  void update(ConditionNode conditionNode);

  void delete(ConditionNode conditionNode);
}
