package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.ClosingPage;

@Slf4j
@Repository
public class ClosingPageDaoImpl implements ClosingPageDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<ClosingPage> findBySurveyId(UUID surveyId) {
    return entityManager.createQuery(
            """
            FROM ClosingPage cp 
            WHERE cp.survey.id = :surveyId
            """, ClosingPage.class)
        .setParameter("surveyId", surveyId)
        .getResultList()
        .stream()
        .findFirst();
  }

  @Override
  public void save(ClosingPage closingPage) {
    log.debug("Сохранена страница завершения для опроса id={}", closingPage.getSurvey().getId());
    entityManager.persist(closingPage);
  }

  @Override
  public void update(ClosingPage closingPage) {
    log.debug("Обновлена страница завершения для опроса id={}", closingPage.getSurvey().getId());
    entityManager.merge(closingPage);
  }

  @Override
  public void delete(ClosingPage closingPage) {
    log.debug("Удалена страница завершения для опроса id={}", closingPage.getSurvey().getId());
    entityManager.remove(closingPage);
  }

  @Override
  public boolean existsBySurveyId(UUID surveyId) {
    Long count = entityManager.createQuery(
            """
            SELECT COUNT(cp) 
            FROM ClosingPage cp 
            WHERE cp.survey.id = :surveyId
            """, Long.class)
        .setParameter("surveyId", surveyId)
        .getSingleResult();
    return count > 0;
  }
}
