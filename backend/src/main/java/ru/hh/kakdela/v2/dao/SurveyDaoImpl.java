package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.Survey;

@Slf4j
@Repository
public class SurveyDaoImpl implements SurveyDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Survey> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(Survey.class, id));
  }

  @Override
  public Optional<UUID> findAuthorIdById(UUID id) {
    return Optional.ofNullable(entityManager.createQuery(
          """
          SELECT s.author.id
          FROM Survey s
          WHERE s.id = :id
          """, UUID.class)
        .setParameter("id", id)
        .getSingleResultOrNull());
  }

  @Override
  public Optional<Boolean> findIsTemplateById(UUID id) {
    return Optional.ofNullable(entityManager.createQuery(
        """
        SELECT s.isTemplate
        FROM Survey s
        WHERE s.id = :id
        """, Boolean.class)
        .setParameter("id", id)
        .getSingleResultOrNull());
  }

  @Override
  public boolean existsById(UUID id) {
    return entityManager
        .createQuery(
        """
        SELECT COUNT(s)
        FROM Survey s
        WHERE s.id = :id
        """, Long.class)
        .setParameter("id", id)
        .getSingleResult()
        .equals(1L);
  }

  @Override
  public List<Survey> findAllByAuthorId(UUID authorId) {
    return entityManager
        .createQuery("FROM Survey s WHERE s.author.id = :authorId", Survey.class)
        .setParameter("authorId", authorId)
        .getResultList();
  }

  @Override
  public void save(Survey survey) {
    log.debug("Сохранен опрос id={}", survey.getId());
    entityManager.persist(survey);
  }

  @Override
  public void update(Survey survey) {
    log.debug("Изменен опрос id={}", survey.getId());
    entityManager.merge(survey);
  }

  @Override
  public void delete(Survey survey) {
    log.debug("Удален опрос id={}", survey.getId());
    entityManager.remove(survey);
  }
}
