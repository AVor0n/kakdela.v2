package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.SurveySubscription;

public interface SurveySubscriptionDao {

  void addSubscription(SurveySubscription subscription);

  void deleteSubscription(SurveySubscription subscription);

  List<Account> findSubscribersBySurveyId(UUID surveyId);

  boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

  Optional<SurveySubscription> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId);
}
