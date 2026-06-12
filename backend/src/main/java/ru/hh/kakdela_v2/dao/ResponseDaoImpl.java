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
  public List<Response> findAllBySurveyId(UUID surveyId) {
    return entityManager
            .createQuery("FROM Response r WHERE r.survey.id = :surveyId", Response.class)
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
  public boolean existsByAccountIdAndSurveyId(UUID accountId, UUID surveyId) {
    return Optional.of(entityManager
                    .createQuery("""
                            SELECT COUNT(r) FROM Response r
                            WHERE r.account.id = :accountId AND r.survey.id = :surveyId
                            """, Long.class)
                    .setParameter("accountId", accountId)
                    .setParameter("surveyId", surveyId)
                    .getSingleResult())
            .map(count -> count > 0)
            .orElse(false);
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
