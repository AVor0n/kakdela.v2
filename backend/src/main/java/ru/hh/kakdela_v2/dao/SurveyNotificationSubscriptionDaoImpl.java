package ru.hh.kakdela_v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyNotificationSubscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SurveyNotificationSubscriptionDaoImpl implements SurveyNotificationSubscriptionDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addSubscription(SurveyNotificationSubscription subscription) {
        entityManager.persist(subscription);
    }

    @Override
    public void deleteSubscription(SurveyNotificationSubscription subscription) {
        entityManager.remove(subscription);
}

    @Override
    public List<UUID> findSubscriberIdsBySurveyId(UUID surveyId) {
        return entityManager.createQuery(
            "SELECT s.account.id FROM SurveyNotificationSubscription s " +
            "WHERE s.survey.id = :surveyId",
            UUID.class
        )
        .setParameter("surveyId", surveyId)
        .getResultList();
    }

    @Override
    public List<Account> findSubscribersBySurveyId(UUID surveyId) {
        return entityManager.createQuery(
            "SELECT s.account FROM SurveyNotificationSubscription s " +
            "WHERE s.survey.id = :surveyId",
            Account.class
        )
        .setParameter("surveyId", surveyId)
        .getResultList();
    }

    @Override
    public boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(s) FROM SurveyNotificationSubscription s " +
            "WHERE s.survey.id = :surveyId AND s.account.id = :accountId",
            Long.class
        )
        .setParameter("surveyId", surveyId)
        .setParameter("accountId", accountId)
        .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public Optional<SurveyNotificationSubscription> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
        return entityManager.createQuery(
            "SELECT s FROM SurveyNotificationSubscription s " +
            "WHERE s.survey.id = :surveyId AND s.account.id = :accountId",
            SurveyNotificationSubscription.class
        )
        .setParameter("surveyId", surveyId)
        .setParameter("accountId", accountId)
        .getResultList()
        .stream()
        .findFirst();
    }
}