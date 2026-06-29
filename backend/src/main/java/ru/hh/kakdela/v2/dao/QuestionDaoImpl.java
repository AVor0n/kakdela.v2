package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import ru.hh.kakdela.v2.model.Question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class QuestionDaoImpl implements QuestionDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Question> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(Question.class, id));
  }

  @Override
  public List<Question> findAllByPageId(UUID pageId) {
    return entityManager
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
    log.debug("Сохранен вопрос id={}", question.getId());
    entityManager.persist(question);
  }

  @Override
  public void update(Question question) {
    log.debug("Изменен вопрос id={}", question.getId());
    entityManager.merge(question);
  }

  @Override
  public void delete(Question question) {
    log.debug("Удален вопрос id={}", question.getId());
    entityManager.remove(question);
  }

  @Override
  public boolean existsByPageIdAndSerialNumber(UUID pageId, Integer serialNumber) {
    return Optional.of(entityManager
                    .createQuery("""
                            SELECT COUNT(q) FROM Question q
                            WHERE q.surveyPage.id = :pageId AND q.serialNumber = :serialNumber
                            """, Long.class)
                    .setParameter("pageId", pageId)
                    .setParameter("serialNumber", serialNumber)
                    .getSingleResultOrNull())
            .map(count -> count > 0)
            .orElse(false);
  }
}
