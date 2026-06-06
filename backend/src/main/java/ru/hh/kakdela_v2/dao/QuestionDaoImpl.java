package ru.hh.kakdela_v2.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.hh.kakdela_v2.model.Question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class QuestionDaoImpl implements QuestionDao {

  private final SessionFactory sessionFactory;

  public QuestionDaoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  public Optional<Question> findById(UUID id) {
    return Optional.ofNullable(session().find(Question.class, id));
  }

  @Override
  public List<Question> findAllByPageId(UUID pageId) {
    return session()
            .createQuery("""
                    FROM Question q
                    WHERE q.surveyPage.id = :pageId
                    ORDER BY q.serialNumber
                    """, Question.class)
            .setParameter("pageId", pageId)
            .getResultList();
  }

  @Override
  public void save(Question question) {
    session().persist(question);
  }

  @Override
  public void update(Question question) {
    session().merge(question);
  }

  @Override
  public void delete(UUID id) {
    Question question = session().find(Question.class, id);
    if (question != null) {
      session().remove(question);
    }
  }

  @Override
  public boolean existsByPageIdAndSerialNumber(UUID pageId, Integer serialNumber) {
    return session()
            .createQuery("""
                    SELECT COUNT(q) FROM Question q
                    WHERE q.surveyPage.id = :pageId AND q.serialNumber = :serialNumber
                    """, Long.class)
            .setParameter("pageId", pageId)
            .setParameter("serialNumber", serialNumber)
            .uniqueResultOptional()
            .map(count -> count > 0)
            .orElse(false);
  }
}
