package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.SurveyNotificationSubscriber;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyNotificationSubscriberDao {

    void subscribe(UUID surveyId, UUID accountId);

    void unsubscribe(UUID surveyId, UUID accountId);

    List<UUID> findSubscriberIdsBySurveyId(UUID surveyId);

    boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

    Optional<SurveyNotificationSubscriber> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId);
}
