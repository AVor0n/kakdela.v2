package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.Answer;

@Slf4j
@Repository
public class AnswerDaoImpl implements AnswerDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Answer> findByResponseIdAndQuestion(UUID responseId, UUID questionId) {
    return Optional.ofNullable(entityManager
        .createQuery(
            """
            SELECT DISTINCT a
            FROM Answer a
            LEFT JOIN FETCH a.selectedAnswerOptions
            WHERE a.response.id = :responseId
            AND a.question.id = :questionId
            """, Answer.class)
        .setParameter("responseId", responseId)
        .setParameter("questionId", questionId)
        .getSingleResultOrNull());
  }

  @Override
  public List<Answer> findAllByResponseId(UUID responseId) {
    return entityManager
        .createQuery(
            """
            SELECT DISTINCT a
            FROM Answer a
            LEFT JOIN FETCH a.selectedAnswerOptions
            WHERE a.response.id = :responseId
            ORDER BY a.pageSerialNumber ASC, a.questionSerialNumber ASC
            """, Answer.class)
        .setParameter("responseId", responseId)
        .getResultList();
  }

  @Override
  public void save(Answer answer) {
    log.debug("Сохранен ответ на вопрос id={}", answer.getId());
    entityManager.persist(answer);
  }

  @Override
  public void update(Answer answer) {
    log.debug("Изменен ответ на вопрос id={}", answer.getId());
    entityManager.merge(answer);
  }

  @Override
  public void delete(Answer answer) {
    log.debug("Удален ответ на вопрос id={}", answer.getId());
    entityManager.remove(answer);
  }
}
