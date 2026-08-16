package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.model.condition.Condition;
import ru.hh.kakdela.v2.model.condition.ConditionAtom;
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
  @SuppressWarnings("unchecked")
  public Optional<ConditionNode>
      findByIdWithParentConditionAndParentPageWithAllQuestionsAndNeighbourConditions(UUID id) {
    Optional<ConditionNode> result = Optional.ofNullable(entityManager.createQuery(
        """
        FROM ConditionNode cn
        WHERE cn.id = :id
        """, ConditionNode.class)
        .setParameter("id", id)
        .getSingleResultOrNull());

    if (result.isEmpty()) {
      return result;
    }

    ConditionNode conditionNode = result.get();

    Condition condition = entityManager.createQuery(
        """
        FROM Condition c
        WHERE c.id = :id
        """, Condition.class)
        .setParameter("id", conditionNode.getCondition().getId())
        .getSingleResult();

    SurveyPage surveyPage = entityManager.createQuery(
            """
            SELECT DISTINCT sp
            FROM SurveyPage sp
            LEFT JOIN FETCH sp.conditions cs
            LEFT JOIN FETCH cs.root r
            LEFT JOIN FETCH r.atom
            WHERE sp.id = :id
            """, SurveyPage.class)
        .setParameter("id", condition.getSurveyPage().getId())
        .getSingleResult();

    if (surveyPage.getConditions().isEmpty()) {
      return result;
    }

    Set<UUID> rootNodeIds = surveyPage.getConditions().stream()
        .filter(c -> c.getRoot() != null)
        .map(c -> c.getRoot().getId())
        .collect(Collectors.toSet());

    if (rootNodeIds.isEmpty()) {
      return result;
    }

    UUID[] rootNodeIdArray = rootNodeIds.toArray(UUID[]::new);

    List<UUID> treeNodeIds = entityManager.createNativeQuery(
            """
            WITH RECURSIVE tree AS (
                SELECT id
                FROM unnest(CAST(:rootNodeIds AS uuid[])) AS roots(id)

                UNION ALL

                SELECT current.id
                FROM condition_node current
                JOIN tree
                    ON current.parent_node_id = tree.id
            )
            SELECT id
            FROM tree
            """, UUID.class)
        .setParameter("rootNodeIds", rootNodeIdArray)
        .getResultList();

    entityManager.createQuery(
            """
            SELECT DISTINCT cn
            FROM ConditionNode cn
            LEFT JOIN FETCH cn.childNodes cns
            LEFT JOIN FETCH cns.atom
            WHERE cn.id IN :treeNodeIds
            """, ConditionNode.class)
        .setParameter("treeNodeIds", treeNodeIds)
        .getResultList();

    entityManager.createQuery(
            """
            SELECT DISTINCT ca
            FROM ConditionAtom ca
            LEFT JOIN FETCH ca.question
            WHERE ca.node.id IN :treeNodeIds
            """, ConditionAtom.class)
        .setParameter("treeNodeIds", treeNodeIds)
        .getResultList();

    return result;
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
