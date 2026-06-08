package ru.hh.kakdela_v2.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.hh.kakdela_v2.model.Answer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AnswerDaoImpl implements AnswerDao {

  private final SessionFactory sessionFactory;

  public AnswerDaoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  public Optional<Answer> findById(Answer.AnswerId id) {
    return Optional.ofNullable(session().find(Answer.class, id));
  }

  @Override
  public List<Answer> findAllByResponseId(UUID responseId) {
    return session()
            .createQuery("""
                    FROM Answer a
                    WHERE a.id.responseId = :responseId
                    """, Answer.class)
            .setParameter("responseId", responseId)
            .getResultList();
  }

  @Override
  public void save(Answer answer) {
    session().persist(answer);
  }

  @Override
  public void update(Answer answer) {
    session().merge(answer);
  }

  @Override
  public void delete(Answer.AnswerId id) {
    Answer answer = session().find(Answer.class, id);
    if (answer != null) {
      session().remove(answer);
    }
  }
}
