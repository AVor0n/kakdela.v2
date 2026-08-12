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
public class ConditionDaoImpl implements ConditionDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Condition> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(Condition.class, id));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Condition> findByIdWithWholeTree(UUID id) {
    Optional<Condition> result = Optional.ofNullable(entityManager.find(Condition.class, id));

    if (result.isEmpty() || result.get().getRoot() == null) {
      return result;
    }

    Condition condition = result.get();

    List<UUID> treeNodeIds = entityManager.createNativeQuery(
        """
        WITH RECURSIVE tree AS (
            SELECT :rootNodeId AS id
        
            UNION ALL
        
            SELECT current.id
            FROM condition_node current
            JOIN tree
                ON current.parent_node_id = tree.id
        )
        SELECT id
        FROM tree
        """, UUID.class)
        .setParameter("rootNodeId", condition.getRoot().getId())
        .getResultList();

    entityManager.createQuery(
        """
        SELECT DISTINCT cn
        FROM ConditionNode cn
        LEFT JOIN FETCH cn.atom
        LEFT JOIN FETCH cn.childNodes cns
        LEFT JOIN FETCH cns.atom
        WHERE cn.id IN :treeNodeIds
        """, ConditionNode.class)
        .setParameter("treeNodeIds", treeNodeIds)
        .getResultList();

    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Condition> findByIdWithParentPageWithAllQuestionsAndNeighbourConditions(UUID id) {
    Optional<Condition> result = Optional.ofNullable(entityManager.createQuery(
        """
        FROM Condition c
        WHERE c.id = :id
        """, Condition.class)
        .setParameter("id", id)
        .getSingleResultOrNull());

    if (result.isEmpty()) {
      return result;
    }

    Condition condition = result.get();

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
        SELECT c.surveyPage.survey.id
        FROM Condition c
        WHERE c.id = :id
        """, UUID.class)
        .setParameter("id", id)
        .getSingleResult();
  }

  @Override
  public List<Condition> findAllByPageId(UUID pageId) {
    return entityManager.createQuery(
         """
         FROM Condition c
         WHERE c.surveyPage.id = :pageId
         """, Condition.class)
        .setParameter("pageId", pageId)
        .getResultList();
  }

  public boolean existsByPageIdAndNextPageId(UUID pageId, UUID nextPageId) {
    List<UUID> results = entityManager.createQuery(
        """
        SELECT c.id
        FROM Condition c
        WHERE c.surveyPage.id = :pageId
        AND c.nextPage.id = :nextPageId
        """, UUID.class)
        .setParameter("pageId", pageId)
        .setParameter("nextPageId", nextPageId)
        .setMaxResults(1)
        .getResultList();

    return !results.isEmpty();
  }

  @Override
  public boolean existsBySurveyId(UUID surveyId) {
    List<UUID> results = entityManager.createQuery(
        """
        SELECT c.id
        FROM Condition c
        WHERE c.surveyPage.survey.id = :surveyId
        """, UUID.class)
        .setParameter("surveyId", surveyId)
        .setMaxResults(1)
        .getResultList();

    return !results.isEmpty();
  }

  @Override
  public void makeConditionsConsistentByPageIdAndItsSerialNumber(UUID pageId, int serialNumber) {
    entityManager.createQuery(
        """
        DELETE FROM Condition c
        WHERE c.surveyPage.id = :pageId
        AND c.nextPage.serialNumber < :serialNumber
        """)
        .setParameter("pageId", pageId)
        .setParameter("serialNumber", serialNumber)
        .executeUpdate();
  }

  @Override
  public void save(Condition condition) {
    log.debug("Сохранено условие: id={}", condition.getId());
    entityManager.persist(condition);
  }

  @Override
  public void update(Condition condition) {
    log.debug("Обновлено условие: id={}", condition.getId());
    entityManager.merge(condition);
  }

  @Override
  public void delete(Condition condition) {
    log.debug("Удалено условие: id={}", condition.getId());
    entityManager.remove(condition);
  }
}
