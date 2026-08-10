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
  public List<Survey> findAllByAuthorId(UUID authorId) {
    return entityManager
        .createQuery("FROM Survey s WHERE s.author.id = :authorId", Survey.class)
        .setParameter("authorId", authorId)
        .getResultList();
  }

  @Override
  public List<Survey> findAllPublished() {
    return entityManager
        .createQuery("FROM Survey s WHERE s.isPublished = true", Survey.class)
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
