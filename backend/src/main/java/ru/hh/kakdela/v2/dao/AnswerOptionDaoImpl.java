package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.AnswerOption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    return entityManager
            .createQuery("""
                    FROM AnswerOption o
                    WHERE o.question.id = :questionId
                    ORDER BY o.serialNumber
                    """, AnswerOption.class)
            .setParameter("questionId", questionId)
            .getResultList();
  }

  @Override
  public void save(AnswerOption option) {
    entityManager.persist(option);
  }

  @Override
  public void update(AnswerOption option) {
    entityManager.merge(option);
  }

  @Override
  public void delete(AnswerOption option) {
    entityManager.remove(option);
  }

  @Override
  public void increaseSerialNumbers(UUID questionId, int startSerial) {
     deferConstraint();

        entityManager.createQuery(
                "UPDATE AnswerOption a SET a.serialNumber = a.serialNumber + 1 " +
                "WHERE a.question.id = :questionId AND a.serialNumber >= :startSerial"
        )
        .setParameter("questionId", questionId)
        .setParameter("startSerial", startSerial)
        .executeUpdate();
  }

  @Override
  public void decreaseSerialNumbers(UUID questionId, int startSerial) {
    deferConstraint();

        entityManager.createQuery(
                "UPDATE AnswerOption a SET a.serialNumber = a.serialNumber - 1 " +
                "WHERE a.question.id = :questionId AND a.serialNumber >= :startSerial"
        )
        .setParameter("questionId", questionId)
        .setParameter("startSerial", startSerial)
        .executeUpdate();
  }

   private void deferConstraint() {
        entityManager.createNativeQuery(
                "SET CONSTRAINTS " + CONSTRAINT_NAME + " DEFERRED"
        ).executeUpdate();
    }
}
