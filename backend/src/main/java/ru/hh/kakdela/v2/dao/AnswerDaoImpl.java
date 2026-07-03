package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.Answer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnswerDaoImpl implements AnswerDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Answer> findById(Answer.AnswerId id) {
    return Optional.ofNullable(entityManager.find(Answer.class, id));
  }

  @Override
  public List<Answer> findAllByResponseId(UUID responseId) {
    return entityManager
            .createQuery("FROM Answer a WHERE a.id.responseId = :responseId", Answer.class)
            .setParameter("responseId", responseId)
            .getResultList();
  }

  @Override
  public void save(Answer answer) {
    entityManager.persist(answer);
  }

  @Override
  public void update(Answer answer) {
    entityManager.merge(answer);
  }

  @Override
  public void delete(Answer answer) {
    entityManager.remove(answer);
  }
}
