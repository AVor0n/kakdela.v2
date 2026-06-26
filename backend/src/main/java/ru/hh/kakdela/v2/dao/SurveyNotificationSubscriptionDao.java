package ru.hh.kakdela.v2.dao;

import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.SurveyNotificationSubscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyNotificationSubscriptionDao {

    void addSubscription(SurveyNotificationSubscription subscription);

    void deleteSubscription(SurveyNotificationSubscription subscription);

    List<UUID> findSubscriberIdsBySurveyId(UUID surveyId);

    List<Account> findSubscribersBySurveyId(UUID surveyId);

    boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

    Optional<SurveyNotificationSubscription> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId);
}
