package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.SurveyPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SurveyPageDaoImpl implements SurveyPageDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<SurveyPage> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(SurveyPage.class, id));
  }

  @Override
  public List<SurveyPage> findAllBySurveyId(UUID surveyId) {
    return entityManager
            .createQuery("""
                    FROM SurveyPage p
                    WHERE p.survey.id = :surveyId
                    ORDER BY p.serialNumber
                    """, SurveyPage.class)
            .setParameter("surveyId", surveyId)
            .getResultList();
  }

  @Override
  public void save(SurveyPage page) {
    entityManager.persist(page);
  }

  @Override
  public void update(SurveyPage page) {
    entityManager.merge(page);
  }

  @Override
  public void delete(SurveyPage page) {
    entityManager.remove(page);
  }

  @Override
  public boolean existsBySurveyIdAndSerialNumber(UUID surveyId, Integer serialNumber) {
    return Optional.of(entityManager
                    .createQuery("""
                            SELECT COUNT(p) FROM SurveyPage p
                            WHERE p.survey.id = :surveyId AND p.serialNumber = :serialNumber
                            """, Long.class)
                    .setParameter("surveyId", surveyId)
                    .setParameter("serialNumber", serialNumber)
                    .getSingleResultOrNull())
            .map(count -> count > 0)
            .orElse(false);
  }
}
