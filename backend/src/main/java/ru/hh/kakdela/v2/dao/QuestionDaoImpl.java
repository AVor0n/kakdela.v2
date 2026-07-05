package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.Question;

@Repository
public class QuestionDaoImpl implements QuestionDao {

  @PersistenceContext
  private EntityManager entityManager;

  private static final String CONSTRAINT_NAME = "uq_question_page_serial";

  @Override
  public Optional<Question> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(Question.class, id));
  }

  @Override
  public List<Question> findAllByPageId(UUID pageId) {
    return entityManager.createQuery(
            """
            FROM Question q
            WHERE q.surveyPage.id = :pageId
            ORDER BY q.serialNumber
            """, Question.class)
        .setParameter("pageId", pageId)
        .getResultList();
  }

  @Override
  public void save(Question question) {
    entityManager.persist(question);
  }

  @Override
  public void update(Question question) {
    entityManager.merge(question);
  }

  @Override
  public void delete(Question question) {
    entityManager.remove(question);
  }

  @Override
  public void increaseSerialNumbers(UUID pageId, int startSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE Question q
            SET q.serialNumber = q.serialNumber + 1
            WHERE q.surveyPage.id = :pageId
              AND q.serialNumber >= :startSerial
            """)
        .setParameter("pageId", pageId)
        .setParameter("startSerial", startSerial)
        .executeUpdate();
  }

  @Override
  public void increaseSerialNumbers(UUID pageId, int startSerial, int endSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE Question q
            SET q.serialNumber = q.serialNumber + 1
            WHERE q.surveyPage.id = :pageId
              AND q.serialNumber BETWEEN :startSerial AND :endSerial
            """)
        .setParameter("pageId", pageId)
        .setParameter("startSerial", startSerial)
        .setParameter("endSerial", endSerial)
        .executeUpdate();
  }

  @Override
  public void decreaseSerialNumbers(UUID pageId, int startSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE Question q
            SET q.serialNumber = q.serialNumber - 1
            WHERE q.surveyPage.id = :pageId
              AND q.serialNumber >= :startSerial
            """)
        .setParameter("pageId", pageId)
        .setParameter("startSerial", startSerial)
        .executeUpdate();
  }

  @Override
  public void decreaseSerialNumbers(UUID pageId, int startSerial, int endSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE Question q
            SET q.serialNumber = q.serialNumber - 1
            WHERE q.surveyPage.id = :pageId
              AND q.serialNumber BETWEEN :startSerial AND :endSerial
            """)
        .setParameter("pageId", pageId)
        .setParameter("startSerial", startSerial)
        .setParameter("endSerial", endSerial)
        .executeUpdate();
  }

  @Override
  public int findMaxSerialNumber(UUID pageId) {
    Integer max = entityManager.createQuery(
            """
            SELECT MAX(q.serialNumber)
            FROM Question q
            WHERE q.surveyPage.id = :pageId
            """, Integer.class)
        .setParameter("pageId", pageId)
        .getSingleResultOrNull();
    return max != null ? max : 0;
  }

  private void deferConstraint() {
    entityManager.createNativeQuery(
        "SET CONSTRAINTS " + CONSTRAINT_NAME + " DEFERRED")
        .executeUpdate();
  }
}
