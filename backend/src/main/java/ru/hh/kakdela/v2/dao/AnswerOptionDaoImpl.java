package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import ru.hh.kakdela.v2.model.AnswerOption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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
    log.debug("Сохранен вариант ответа id={}", option.getId());
    entityManager.persist(option);
  }

  @Override
  public void update(AnswerOption option) {
    log.debug("Изменен вариант ответа id={}", option.getId());
    entityManager.merge(option);
  }

  @Override
  public void delete(AnswerOption option) {
    log.debug("Удален вариант ответа id={}", option.getId());
    entityManager.remove(option);
  }
}
