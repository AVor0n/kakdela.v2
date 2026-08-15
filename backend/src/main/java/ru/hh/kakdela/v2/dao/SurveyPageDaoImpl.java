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
import ru.hh.kakdela.v2.model.condition.ConditionNode;

@Slf4j
@Repository
public class SurveyPageDaoImpl implements SurveyPageDao {

  @PersistenceContext
  private EntityManager entityManager;

  private static final String CONSTRAINT_NAME = "uk_page_survey_serial";

  @Override
  public Optional<SurveyPage> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(SurveyPage.class, id));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<SurveyPage>
      findByIdWithAllConditionsAndParentSurveyWithPagesAndQuestions(UUID id) {
    Optional<SurveyPage> result = Optional.ofNullable(entityManager.createQuery(
        """
        SELECT DISTINCT sp
        FROM SurveyPage sp
        LEFT JOIN FETCH sp.conditions cs
        LEFT JOIN FETCH cs.root r
        LEFT JOIN FETCH r.atom
        WHERE sp.id = :id
        """, SurveyPage.class)
        .setParameter("id", id)
        .getSingleResultOrNull());

    if (result.isEmpty()) {
      return result;
    }

    SurveyPage surveyPage = result.get();

    entityManager.createQuery(
        """
        SELECT DISTINCT sp
        FROM SurveyPage sp
        LEFT JOIN FETCH sp.survey s
        LEFT JOIN FETCH s.pages
        WHERE sp.id = :id
        """, SurveyPage.class)
        .setParameter("id", id)
        .getSingleResultOrNull();

    Set<UUID> pageIds = surveyPage.getSurvey().getPages().stream()
        .map(SurveyPage::getId)
        .collect(Collectors.toSet());

    entityManager.createQuery(
        """
        SELECT DISTINCT sp
        FROM SurveyPage sp
        LEFT JOIN FETCH sp.questions
        WHERE sp.id IN :pageIds
        """, SurveyPage.class)
        .setParameter("pageIds", pageIds)
        .getResultList();

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
  public List<SurveyPage> findAllBySurveyId(UUID surveyId) {
    return entityManager
        .createQuery(
            """
            FROM SurveyPage p
            WHERE p.survey.id = :surveyId
            ORDER BY p.serialNumber
            """, SurveyPage.class)
        .setParameter("surveyId", surveyId)
        .getResultList();
  }

  @Override
  public Optional<SurveyPage> findFirstBySurveyId(UUID surveyId) {
    return Optional.ofNullable(entityManager.createQuery(
        """
        FROM SurveyPage sp
        WHERE sp.survey.id = :surveyId
        AND sp.serialNumber = 1
        """, SurveyPage.class)
        .setParameter("surveyId", surveyId)
        .getSingleResultOrNull());
  }

  @Override
  public void save(SurveyPage page) {
    log.debug("Сохранена страница id={}", page.getId());
    entityManager.persist(page);
  }

  @Override
  public void update(SurveyPage page) {
    log.debug("Изменена страница id={}", page.getId());
    entityManager.merge(page);
  }

  @Override
  public void delete(SurveyPage page) {
    log.debug("Удалена страница id={}", page.getId());
    entityManager.remove(page);
  }

  @Override
  public void increaseSerialNumbers(UUID surveyId, int startSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE SurveyPage p
            SET p.serialNumber = p.serialNumber + 1
            WHERE p.survey.id = :surveyId
              AND p.serialNumber >= :startSerial
            """)
        .setParameter("surveyId", surveyId)
        .setParameter("startSerial", startSerial)
        .executeUpdate();
  }

  @Override
  public void increaseSerialNumbers(UUID surveyId, int startSerial, int endSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE SurveyPage p SET p.serialNumber = p.serialNumber + 1
            WHERE p.survey.id = :surveyId
              AND p.serialNumber BETWEEN :startSerial AND :endSerial
            """)
        .setParameter("surveyId", surveyId)
        .setParameter("startSerial", startSerial)
        .setParameter("endSerial", endSerial)
        .executeUpdate();
  }

  @Override
  public void decreaseSerialNumbers(UUID surveyId, int startSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE SurveyPage p
            SET p.serialNumber = p.serialNumber - 1
            WHERE p.survey.id = :surveyId
              AND p.serialNumber >= :startSerial
            """)
        .setParameter("surveyId", surveyId)
        .setParameter("startSerial", startSerial)
        .executeUpdate();
  }

  @Override
  public void decreaseSerialNumbers(UUID surveyId, int startSerial, int endSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE SurveyPage p
            SET p.serialNumber = p.serialNumber - 1
            WHERE p.survey.id = :surveyId
              AND p.serialNumber BETWEEN :startSerial AND :endSerial
            """)
        .setParameter("surveyId", surveyId)
        .setParameter("startSerial", startSerial)
        .setParameter("endSerial", endSerial)
        .executeUpdate();
  }

  @Override
  public int findMaxSerialNumber(UUID surveyId) {
    Integer max = entityManager.createQuery(
            """
            SELECT MAX(p.serialNumber)
            FROM SurveyPage p
            WHERE p.survey.id = :surveyId
            """, Integer.class)
        .setParameter("surveyId", surveyId)
        .getSingleResultOrNull();

    return max != null ? max : 0;
  }

  private void deferConstraint() {
    entityManager.createNativeQuery(
            "SET CONSTRAINTS " + CONSTRAINT_NAME + " DEFERRED")
        .executeUpdate();
  }
}
