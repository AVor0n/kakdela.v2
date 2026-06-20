package ru.hh.kakdela_v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.SurveyNotificationSettings;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SurveyNotificationSettingsDaoImpl implements SurveyNotificationSettingsDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<SurveyNotificationSettings> findBySurveyId(UUID surveyId) {
        return Optional.ofNullable(entityManager.find(SurveyNotificationSettings.class, surveyId));
    }

    @Override
    public void save(SurveyNotificationSettings settings) {
        entityManager.persist(settings);
    }

    @Override
    public void update(SurveyNotificationSettings settings) {
        entityManager.merge(settings);
    }

    @Override
    public void deleteBySurveyId(UUID surveyId) {
        entityManager.createQuery(
                "DELETE FROM SurveyNotificationSettings s WHERE s.surveyId = :surveyId"
            )
            .setParameter("surveyId", surveyId)
            .executeUpdate();
    }

    @Override
    public boolean existsBySurveyId(UUID surveyId) {
        Long count = entityManager.createQuery(
                "SELECT COUNT(s) FROM SurveyNotificationSettings s WHERE s.surveyId = :surveyId",
                Long.class
            )
            .setParameter("surveyId", surveyId)
            .getSingleResult();
        return count > 0;
    }
}
