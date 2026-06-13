package ru.hh.kakdela_v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.Response;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    return entityManager
            .createQuery("FROM Response r WHERE r.survey.id = :surveyId AND r.isComplete = true", Response.class)
            .setParameter("surveyId", surveyId)
            .getResultList();
  }

  @Override
  public List<Response> findAllByAccountId(UUID accountId) {
    return entityManager
            .createQuery("FROM Response r WHERE r.account.id = :accountId", Response.class)
            .setParameter("accountId", accountId)
            .getResultList();
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
        .getSingleResult();
  }

  @Override
  public long countIncompletedBySurveyId(UUID surveyId) {
    return entityManager
        .createQuery(
            """
            SELECT COUNT(r)
            FROM Response r 
            WHERE r.survey.id = :surveyId AND r.isComplete = false
            """, Long.class)
        .setParameter("surveyId", surveyId)
        .getSingleResult();
  }

  @Override
  public List<Response> findIncompletedBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
    return entityManager
        .createQuery(
            """
            FROM Response r
            WHERE r.account.id = :accountId AND r.survey.id = :surveyId AND r.isComplete = false
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
            WHERE r.account.id = :accountId AND r.survey.id = :surveyId
            """, Long.class)
        .setParameter("accountId", accountId)
        .setParameter("surveyId", surveyId)
        .getSingleResult() > 0;
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
        .getSingleResult()
        .equals(0L);
  }

  @Override
  public void save(Response response) {
    entityManager.persist(response);
  }

  @Override
  public void update(Response response) {
    entityManager.merge(response);
  }

  @Override
  public void delete(Response response) {
    entityManager.remove(response);
  }
}
