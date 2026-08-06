package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.Answer;
import ru.hh.kakdela.v2.model.Response;

@Slf4j
@Repository
public class ResponseDaoImpl implements ResponseDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Response> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(Response.class, id));
  }

  @Override
  public List<Response> findCompletedBySurveyId(UUID surveyId) {
    List<Response> result = entityManager.createQuery(
        """
        SELECT DISTINCT r
        FROM Response r
        LEFT JOIN FETCH r.account
        LEFT JOIN FETCH r.answers
        WHERE r.survey.id = :surveyId
        AND r.isCompleted = true
        """, Response.class)
        .setParameter("surveyId", surveyId)
        .getResultList();

    List<UUID> answerIds = result.stream()
        .flatMap(r -> r.getAnswers().stream())
        .map(Answer::getId)
        .toList();

    entityManager.createQuery(
        """
        SELECT DISTINCT a
        FROM Answer a
        LEFT JOIN FETCH a.selectedAnswerOptions
        WHERE a.id IN :ids
        """, Answer.class)
        .setParameter("ids", answerIds)
        .getResultList();

    return result;
  }

  @Override
  public List<Response> findAllByAccountId(UUID accountId) {
    List<Response> result = entityManager.createQuery(
        """
        SELECT DISTINCT r
        FROM Response r
        LEFT JOIN FETCH r.account
        LEFT JOIN FETCH r.answers a
        WHERE r.account.id = :accountId
        """, Response.class)
        .setParameter("accountId", accountId)
        .getResultList();

    List<UUID> answerIds = result.stream()
        .flatMap(r -> r.getAnswers().stream())
        .map(Answer::getId)
        .toList();

    entityManager.createQuery(
        """
        SELECT DISTINCT a
        FROM Answer a
        LEFT JOIN FETCH a.selectedAnswerOptions
        WHERE a.id IN :ids
        """, Answer.class)
        .setParameter("ids", answerIds)
        .getResultList();

    return result;
  }

  @Override
  public long countAllBySurveyId(UUID surveyId) {
    return entityManager
        .createQuery(
            """
            SELECT COUNT(r)
            FROM Response r
            WHERE r.survey.id = :surveyId
            """, Long.class)
        .setParameter("surveyId", surveyId)
        .getSingleResultOrNull();
  }

  @Override
  public long countIncompletedBySurveyId(UUID surveyId) {
    return entityManager
        .createQuery(
            """
            SELECT COUNT(r)
            FROM Response r
            WHERE r.survey.id = :surveyId
            AND r.isCompleted = false
            """, Long.class)
        .setParameter("surveyId", surveyId)
        .getSingleResultOrNull();
  }

  @Override
  public List<Response> findIncompletedBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
    return entityManager
        .createQuery(
            """
            FROM Response r
            WHERE r.account.id = :accountId
            AND r.survey.id = :surveyId
            AND r.isCompleted = false
            """, Response.class)
        .setParameter("accountId", accountId)
        .setParameter("surveyId", surveyId)
        .getResultList();
  }

  @Override
  public boolean existsBySurveyIdAndAccountId(UUID accountId, UUID surveyId) {
    return entityManager
        .createQuery(
            """
            SELECT COUNT(r) FROM Response r
            WHERE r.account.id = :accountId
            AND r.survey.id = :surveyId
            """, Long.class)
        .setParameter("accountId", accountId)
        .setParameter("surveyId", surveyId)
        .getSingleResultOrNull() > 0;
  }

  @Override
  public boolean areAllMandatoryQuestionsAnswered(UUID responseId) {
    return entityManager
        .createQuery(
            """
            SELECT COUNT(q)
            FROM Question q
            WHERE q.isMandatory = true
            AND q.surveyPage.survey = (
                SELECT r.survey
                FROM Response r
                WHERE r.id = :responseId
            )
            AND q.id NOT IN (
                SELECT a.question.id
                FROM Answer a
                WHERE a.response.id = :responseId
            )
            """, Long.class)
        .setParameter("responseId", responseId)
        .getSingleResultOrNull()
        .equals(0L);
  }

  @Override
  public boolean areAllMandatoryQuestionsOfPageAnswered(UUID responseId, UUID pageId) {
    return entityManager
        .createQuery(
            """
            SELECT COUNT(q)
            FROM Question q
            WHERE q.isMandatory = true
            AND q.surveyPage.id = :pageId
            AND q.id NOT IN (
                SELECT a.question.id
                FROM Answer a
                WHERE a.response.id = :responseId
            )
            """, Long.class)
        .setParameter("responseId", responseId)
        .setParameter("pageId", pageId)
        .getSingleResultOrNull()
        .equals(0L);
  }

  @Override
  public void save(Response response) {
    log.debug("Сохранен ответ на опрос id={}", response.getId());
    entityManager.persist(response);
  }

  @Override
  public void update(Response response) {
    log.debug("Изменен ответ на опрос id={}", response.getId());
    entityManager.merge(response);
  }

  @Override
  public void delete(Response response) {
    log.debug("Удален ответ на опрос id={}", response.getId());
    entityManager.remove(response);
  }
}
