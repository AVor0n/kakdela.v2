package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.condition.ConditionNode;

@Slf4j
@Repository
public class ConditionNodeDaoImpl implements ConditionNodeDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<ConditionNode> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(ConditionNode.class, id));
  }

  @Override
  public Optional<ConditionNode> findByIdWithParentAndGrandparentNodeAndParentCondition(UUID id) {
    return Optional.ofNullable(entityManager.createQuery(
        """
        FROM ConditionNode cn
        LEFT JOIN FETCH cn.condition
        LEFT JOIN FETCH cn.atom
        LEFT JOIN FETCH cn.parentNode cnp
        LEFT JOIN FETCH cnp.atom
        WHERE cn.id = :id
        """, ConditionNode.class)
        .setParameter("id", id)
        .getSingleResultOrNull());
  }

  @Override
  public UUID findParentSurveyIdById(UUID id) {
    return entityManager.createQuery(
        """
        SELECT cn.condition.surveyPage.survey.id
        FROM ConditionNode cn
        WHERE cn.id = :id
        """, UUID.class)
        .setParameter("id", id)
        .getSingleResult();
  }

  @Override
  public boolean doesNodeHaveOneChild(UUID id) {
    return entityManager.createQuery(
        """
        SELECT COUNT(cn)
        FROM ConditionNode cn
        WHERE cn.parentNode.id = :id
        """, Long.class)
        .setParameter("id", id)
        .getSingleResult() == 1L;
  }

  @Override
  public void save(ConditionNode conditionNode) {
    log.debug("Сохранена вершина условия: id={}", conditionNode.getId());
    entityManager.persist(conditionNode);
  }

  @Override
  public void update(ConditionNode conditionNode) {
    log.debug("Обновлена вершина условия: id={}", conditionNode.getId());
    entityManager.merge(conditionNode);
  }

  @Override
  public void delete(ConditionNode conditionNode) {
    log.debug("Удалена вершина условия: id={}", conditionNode.getId());
    entityManager.remove(conditionNode);
  }
}
