package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import ru.hh.kakdela.v2.model.AnswerOption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.AnswerOption;

@Slf4j
@Repository
public class AnswerOptionDaoImpl implements AnswerOptionDao {

  @PersistenceContext
  private EntityManager entityManager;

  private static final String CONSTRAINT_NAME = "uq_answer_option_question_serial";

  @Override
  public Optional<AnswerOption> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(AnswerOption.class, id));
  }

  @Override
  public List<AnswerOption> findAllByQuestionId(UUID questionId) {
    return entityManager.createQuery(
            """
            FROM AnswerOption o
            WHERE o.question.id = :questionId
            ORDER BY o.serialNumber
            """, AnswerOption.class)
        .setParameter("questionId", questionId)
        .getResultList();
  }

  @Override
  public void save(AnswerOption option) {
    log.debug("Сохранен вариант ответа id={}", option.getId());
    entityManager.persist(option);
  }

  @Override
  public void update(AnswerOption option) {
    log.debug("Изменен вариант ответа id={}", option.getId());
    entityManager.merge(option);
  }

  @Override
  public void delete(AnswerOption option) {
    log.debug("Удален вариант ответа id={}", option.getId());
    entityManager.remove(option);
  }

  @Override
  public void increaseSerialNumbers(UUID questionId, int startSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE AnswerOption a
            SET a.serialNumber = a.serialNumber + 1
            WHERE a.question.id = :questionId
              AND a.serialNumber >= :startSerial
            """)
        .setParameter("questionId", questionId)
        .setParameter("startSerial", startSerial)
        .executeUpdate();
  }

  @Override
  public void increaseSerialNumbers(UUID questionId, int startSerial, int endSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE AnswerOption a
            SET a.serialNumber = a.serialNumber + 1
            WHERE a.question.id = :questionId
              AND a.serialNumber BETWEEN :startSerial AND :endSerial
            """)
        .setParameter("questionId", questionId)
        .setParameter("startSerial", startSerial)
        .setParameter("endSerial", endSerial)
        .executeUpdate();
  }

  @Override
  public void decreaseSerialNumbers(UUID questionId, int startSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE AnswerOption a
            SET a.serialNumber = a.serialNumber - 1
            WHERE a.question.id = :questionId
              AND a.serialNumber >= :startSerial
            """)
        .setParameter("questionId", questionId)
        .setParameter("startSerial", startSerial)
        .executeUpdate();
  }

  @Override
  public void decreaseSerialNumbers(UUID questionId, int startSerial, int endSerial) {
    deferConstraint();

    entityManager.createQuery(
            """
            UPDATE AnswerOption a
            SET a.serialNumber = a.serialNumber - 1
            WHERE a.question.id = :questionId
              AND a.serialNumber BETWEEN :startSerial AND :endSerial
            """)
        .setParameter("questionId", questionId)
        .setParameter("startSerial", startSerial)
        .setParameter("endSerial", endSerial)
        .executeUpdate();
  }

  @Override
  public int findMaxSerialNumber(UUID questionId) {
    Integer max = entityManager.createQuery(
            """
            SELECT MAX(a.serialNumber)
            FROM AnswerOption a
            WHERE a.question.id = :questionId
            """, Integer.class)
        .setParameter("questionId", questionId)
        .getSingleResultOrNull();
    return max != null ? max : 0;
  }

  private void deferConstraint() {
    entityManager.createNativeQuery(
        "SET CONSTRAINTS " + CONSTRAINT_NAME + " DEFERRED")
        .executeUpdate();
  }
}
