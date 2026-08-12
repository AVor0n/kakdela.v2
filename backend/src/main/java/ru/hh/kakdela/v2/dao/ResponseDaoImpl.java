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
  public Optional<Response> findByIdWithAllAnswersAndPageStatuses(UUID id) {
    Optional<Response> result = Optional.ofNullable(entityManager.createQuery(
          """
          SELECT DISTINCT r
          FROM Response r
          LEFT JOIN FETCH r.answers
          WHERE r.id = :id
          """, Response.class)
        .setParameter("id", id)
        .getSingleResultOrNull());

    if (result.isEmpty()) {
      return result;
    }

    Response response = result.get();

    entityManager.createQuery(
          """
          SELECT DISTINCT r
          FROM Response r
          LEFT JOIN FETCH r.pageStatuses
          WHERE r.id = :id
          """, Response.class)
        .setParameter("id", id)
        .getSingleResultOrNull();

    if (response.getAnswers().isEmpty()) {
      return result;
    }

    Set<UUID> answerIds = response.getAnswers().stream()
        .map(Answer::getId)
        .collect(Collectors.toSet());

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
  public Optional<Response> findByIdWithPageStatuses(UUID id) {
    return Optional.ofNullable(entityManager.createQuery(
            """
            SELECT DISTINCT r
            FROM Response r
            LEFT JOIN FETCH r.pageStatuses
            WHERE r.id = :id
            """, Response.class)
        .setParameter("id", id)
        .getSingleResultOrNull());
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

    Set<UUID> answerIds = result.stream()
        .flatMap(r -> r.getAnswers().stream())
        .map(Answer::getId)
        .collect(Collectors.toSet());

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

    Set<UUID> answerIds = result.stream()
        .flatMap(r -> r.getAnswers().stream())
        .map(Answer::getId)
        .collect(Collectors.toSet());

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
  public boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
    List<UUID> results = entityManager.createQuery(
        """
        SELECT r.id
        FROM Response r
        WHERE r.survey.id = :surveyId
        AND r.account.id = :accountId
        """, UUID.class)
        .setParameter("surveyId", surveyId)
        .setParameter("accountId", accountId)
        .setMaxResults(1)
        .getResultList();

    return !results.isEmpty();
  }

  @Override
  public boolean areAllMandatoryQuestionsAnswered(UUID responseId) {
    List<UUID> result = entityManager.createQuery(
        """
        SELECT q.id
        FROM Question q
        JOIN ResponsePageStatus rsp ON rsp.surveyPage.id = q.surveyPage.id
        LEFT JOIN Answer a ON a.question.id = q.id AND a.response.id = :responseId
        WHERE q.isMandatory = true
        AND rsp.response.id = :responseId
        AND rsp.isIncluded = true
        AND a.id IS NULL
        """, UUID.class)
        .setParameter("responseId", responseId)
        .setMaxResults(1)
        .getResultList();

    return result.isEmpty();
  }

  @Override
  public boolean areAllMandatoryQuestionsOfPageAnswered(UUID responseId, UUID pageId) {
    List<UUID> result = entityManager.createQuery(
        """
        SELECT q.id
        FROM Question q
        LEFT JOIN Answer a ON a.question.id = q.id AND a.response.id = :responseId
        WHERE q.isMandatory = true
        AND q.surveyPage.id = :pageId
        AND a.id IS NULL
        """, UUID.class)
        .setParameter("responseId", responseId)
        .setParameter("pageId", pageId)
        .setMaxResults(1)
        .getResultList();

    return result.isEmpty();
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
