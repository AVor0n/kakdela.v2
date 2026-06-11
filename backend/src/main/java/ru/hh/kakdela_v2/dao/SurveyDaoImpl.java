package ru.hh.kakdela_v2.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.Survey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SurveyDaoImpl implements SurveyDao {

  private final SessionFactory sessionFactory;

  public SurveyDaoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  public Optional<Survey> findById(UUID id) {
    return Optional.ofNullable(session().find(Survey.class, id))
  }

  @Override
  public List<Survey> findAllByAuthorId(UUID authorId) {
    return session()
            .createQuery("FROM Survey s WHERE s.author.id = :authorId", Survey.class)
            .setParameter("authorId", authorId)
            .getResultList();
  }

  @Override
  public List<Survey> findAllPublished() {
    return session()
            .createQuery("FROM Survey s WHERE s.isPublished = true", Survey.class)
            .getResultList();
  }

  @Override
  public void save(Survey survey) {
    session().persist(survey);
  }

  @Override
  public void update(Survey survey) {
    session().merge(survey);
  }

  @Override
  public void delete(UUID id) {
    Survey survey = session().find(Survey.class, id);
    if (survey != null) {
      session().remove(survey);
    }
  }
}
