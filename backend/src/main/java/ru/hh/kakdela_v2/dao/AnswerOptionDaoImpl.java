package ru.hh.kakdela_v2.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.AnswerOption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnswerOptionDaoImpl implements AnswerOptionDao {

  private final SessionFactory sessionFactory;

  public AnswerOptionDaoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  public Optional<AnswerOption> findById(UUID id) {
    return Optional.ofNullable(session().find(AnswerOption.class, id));
  }

  @Override
  public List<AnswerOption> findAllByQuestionId(UUID questionId) {
    return session()
            .createQuery("""
                    FROM AnswerOption o
                    WHERE o.question.id = :questionId
                    ORDER BY o.serialNumber
                    """, AnswerOption.class)
            .setParameter("questionId", questionId)
            .getResultList();
  }

  @Override
  public void save(AnswerOption option) {
    session().persist(option);
  }

  @Override
  public void update(AnswerOption option) {
    session().merge(option);
  }

  @Override
  public void delete(UUID id) {
    AnswerOption option = session().find(AnswerOption.class, id);
    if (option != null) {
      session().remove(option);
    }
  }
}
