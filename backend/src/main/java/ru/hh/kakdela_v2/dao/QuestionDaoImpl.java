package ru.hh.kakdela_v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.Question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    entityManager.persist(question);
  }

  @Override
  public void update(Question question) {
    entityManager.merge(question);
  }

  @Override
  public void delete(Question question) {
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
                    .getSingleResult())
            .map(count -> count > 0)
            .orElse(false);
  }
}
