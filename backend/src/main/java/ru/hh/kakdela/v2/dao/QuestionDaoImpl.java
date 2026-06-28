package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.Question;

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
                    .getSingleResultOrNull())
            .map(count -> count > 0)
            .orElse(false);
  }

  @Override
  public void shiftSerialNumbersUp(UUID pageId, int startSerial, int shift) {
    String sql = "UPDATE question q " +
        "SET serial_number = q.serial_number + ? " +
        "FROM (SELECT id FROM question " +
        "      WHERE survey_page_id = ? AND serial_number >= ? " +
        "      ORDER BY serial_number DESC) AS sub " +
        "WHERE q.id = sub.id";

    entityManager.createNativeQuery(sql)
        .setParameter(1, shift)
        .setParameter(2, pageId)
        .setParameter(3, startSerial)
        .executeUpdate();
  }

  @Override
  public void shiftSerialNumbersDown(UUID pageId, int startSerial, int shift) {
    String sql = "UPDATE question q " +
        "SET serial_number = q.serial_number + ? " +
        "FROM (SELECT id FROM question " +
        "      WHERE survey_page_id = ? AND serial_number >= ? " +
        "      ORDER BY serial_number ASC) AS sub " +
        "WHERE q.id = sub.id";

    entityManager.createNativeQuery(sql)
        .setParameter(1, shift)
        .setParameter(2, pageId)
        .setParameter(3, startSerial)
        .executeUpdate();
  }

}
