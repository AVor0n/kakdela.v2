package ru.hh.kakdela_v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.kakdela_v2.model.SurveyNotificationSubscriber;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SurveyNotificationSubscriberDaoImpl implements SurveyNotificationSubscriberDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void subscribe(UUID surveyId, UUID accountId) {
        if (existsBySurveyIdAndAccountId(surveyId, accountId)) {
            return;
        }

        entityManager.createNativeQuery(
            "INSERT INTO survey_notification_subscribers (survey_id, account_id) VALUES (?, ?)"
        )
        .setParameter(1, surveyId)
        .setParameter(2, accountId)
        .executeUpdate();
    }

    @Override
    public void unsubscribe(UUID surveyId, UUID accountId) {
        entityManager.createQuery(
            "DELETE FROM SurveyNotificationSubscriber s " +
            "WHERE s.survey.id = :surveyId AND s.account.id = :accountId"
        )
        .setParameter("surveyId", surveyId)
        .setParameter("accountId", accountId)
        .executeUpdate();
    }

    @Override
    public List<UUID> findSubscriberIdsBySurveyId(UUID surveyId) {
        return entityManager.createQuery(
            "SELECT s.account.id FROM SurveyNotificationSubscriber s " +
            "WHERE s.survey.id = :surveyId",
            UUID.class
        )
        .setParameter("surveyId", surveyId)
        .getResultList();
    }

    @Override
    public boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(s) FROM SurveyNotificationSubscriber s " +
            "WHERE s.survey.id = :surveyId AND s.account.id = :accountId",
            Long.class
        )
        .setParameter("surveyId", surveyId)
        .setParameter("accountId", accountId)
        .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public Optional<SurveyNotificationSubscriber> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
        return entityManager.createQuery(
            "SELECT s FROM SurveyNotificationSubscriber s " +
            "WHERE s.survey.id = :surveyId AND s.account.id = :accountId",
            SurveyNotificationSubscriber.class
        )
        .setParameter("surveyId", surveyId)
        .setParameter("accountId", accountId)
        .getResultList()
        .stream()
        .findFirst();
    }
}