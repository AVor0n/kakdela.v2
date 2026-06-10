package ru.hh.kakdela_v2.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.Response;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ResponseDaoImpl implements ResponseDao {

  private final SessionFactory sessionFactory;

  public ResponseDaoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  public Optional<Response> findById(UUID id) {
    return session()
            .createQuery("""
                    SELECT r FROM Response r
                    LEFT JOIN FETCH r.answers
                    WHERE r.id = :id
                    """, Response.class)
            .setParameter("id", id)
            .uniqueResultOptional();
  }

  @Override
  public List<Response> findAllBySurveyId(UUID surveyId) {
    return session()
            .createQuery("FROM Response r WHERE r.survey.id = :surveyId", Response.class)
            .setParameter("surveyId", surveyId)
            .getResultList();
  }

  @Override
  public List<Response> findAllByAccountId(UUID accountId) {
    return session()
            .createQuery("FROM Response r WHERE r.account.id = :accountId", Response.class)
            .setParameter("accountId", accountId)
            .getResultList();
  }

  @Override
  public boolean existsByAccountIdAndSurveyId(UUID accountId, UUID surveyId) {
    return session()
            .createQuery("""
                    SELECT COUNT(r) FROM Response r
                    WHERE r.account.id = :accountId AND r.survey.id = :surveyId
                    """, Long.class)
            .setParameter("accountId", accountId)
            .setParameter("surveyId", surveyId)
            .uniqueResultOptional()
            .map(count -> count > 0)
            .orElse(false);
  }

  @Override
  public void save(Response response) {
    session().persist(response);
  }

  @Override
  public void update(Response response) {
    session().merge(response);
  }

  @Override
  public void delete(UUID id) {
    Response response = session().find(Response.class, id);
    if (response != null) {
      session().remove(response);
    }
  }
}
