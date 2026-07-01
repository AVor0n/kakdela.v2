package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.AnswerOption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnswerOptionDaoImpl implements AnswerOptionDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<AnswerOption> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(AnswerOption.class, id));
  }

  @Override
  public List<AnswerOption> findAllByQuestionId(UUID questionId) {
    return entityManager
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
    entityManager.persist(option);
  }

  @Override
  public void update(AnswerOption option) {
    entityManager.merge(option);
  }

  @Override
  public void delete(AnswerOption option) {
    entityManager.remove(option);
  }
}
