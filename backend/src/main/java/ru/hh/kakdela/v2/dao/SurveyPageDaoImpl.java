package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import ru.hh.kakdela.v2.model.SurveyPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.SurveyPage;

@Slf4j
@Repository
public class SurveyPageDaoImpl implements SurveyPageDao {

  @PersistenceContext
  private EntityManager entityManager;

  private static final String CONSTRAINT_NAME = "uq_page_survey_serial";

  @Override
  public Optional<SurveyPage> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(SurveyPage.class, id));
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
