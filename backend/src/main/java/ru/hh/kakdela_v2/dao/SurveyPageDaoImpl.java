package ru.hh.kakdela_v2.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.SurveyPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SurveyPageDaoImpl implements SurveyPageDao {

  private final SessionFactory sessionFactory;

  public SurveyPageDaoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  public Optional<SurveyPage> findById(UUID id) {
    return Optional.ofNullable(session().find(SurveyPage.class, id));
  }

  @Override
  public List<SurveyPage> findAllBySurveyId(UUID surveyId) {
    return session()
            .createQuery("""
                    FROM SurveyPage p
                    WHERE p.survey.id = :surveyId
                    ORDER BY p.serialNumber
                    """, SurveyPage.class)
            .setParameter("surveyId", surveyId)
            .getResultList();
  }

  @Override
  public void save(SurveyPage page) {
    session().persist(page);
  }

  @Override
  public void update(SurveyPage page) {
    session().merge(page);
  }

  @Override
  public void delete(UUID id) {
    SurveyPage page = session().find(SurveyPage.class, id);
    if (page != null) {
      session().remove(page);
    }
  }

  @Override
  public boolean existsBySurveyIdAndSerialNumber(UUID surveyId, Integer serialNumber) {
    return session()
            .createQuery("""
                    SELECT COUNT(p) FROM SurveyPage p
                    WHERE p.survey.id = :surveyId AND p.serialNumber = :serialNumber
                    """, Long.class)
            .setParameter("surveyId", surveyId)
            .setParameter("serialNumber", serialNumber)
            .uniqueResultOptional()
            .map(count -> count > 0)
            .orElse(false);
  }
}
