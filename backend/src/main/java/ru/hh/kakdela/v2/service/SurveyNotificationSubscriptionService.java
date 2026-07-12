package ru.hh.kakdela.v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dao.SurveyNotificationSubscriptionDao;
import ru.hh.kakdela.v2.dto.subscription.SubscriptionResponseDto;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Permission.SurveyRole;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyNotificationSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyNotificationSubscriptionService {

  private final SurveyNotificationSubscriptionDao subscriptionDao;
  private final AccountDao accountDao;
  private final SurveyDao surveyDao;
  private final PermissionService permissionService;
  private final EmailService emailService;

  @Transactional
  public SubscriptionResponseDto subscribeUsers(
      UUID surveyId, List<String> emails, UUID currentUserId) {
    permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);

    List<String> subscribedEmails = new ArrayList<>();
    List<String> alreadySubscribedEmails = new ArrayList<>();
    List<String> notFoundEmails = new ArrayList<>();

    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    for (String email : emails) {
      try {
        Account account = findAccountByEmailOrThrow(email);
        UUID accountId = account.getId();

        if (subscriptionDao.existsBySurveyIdAndAccountId(surveyId, accountId)) {
          alreadySubscribedEmails.add(email);
          log.debug("Пользователь {} уже подписан", email);
          continue;
        }

        SurveyNotificationSubscription subscription = SurveyNotificationSubscription.builder()
            .survey(survey)
            .account(account)
            .build();

        subscriptionDao.addSubscription(subscription);
        subscribedEmails.add(email);

        if (survey.isPublished()) {
          emailService.sendSurveyPublishedEmail(email, survey.getTitle(), surveyId);
        }

        log.info("Пользователь {} подписан на опрос {}", email, surveyId);
      } catch (ResponseStatusException e) {
        notFoundEmails.add(email);
        log.warn("Пользователь с адресом электронной почты {} не найден", email);
      }
    }

    log.info("Подписано {} пользователей на опрос {}", subscribedEmails.size(), surveyId);
    return new SubscriptionResponseDto(
        subscribedEmails,
        alreadySubscribedEmails,
        notFoundEmails
    );
  }

  @Transactional
  public void unsubscribeUser(UUID surveyId, String email, UUID currentUserId) {
    permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);

    Account account = findAccountByEmailOrThrow(email);
    UUID accountId = account.getId();

    SurveyNotificationSubscription subscription = subscriptionDao
        .findBySurveyIdAndAccountId(surveyId, accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Подписка для " + email + " не найдена"));

    subscriptionDao.deleteSubscription(subscription);
    log.info("Пользователь {} отписался от опроса {}", email, surveyId);
  }

  @Transactional(readOnly = true)
  public List<Account> getSubscribers(UUID surveyId, UUID currentUserId) {
    permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);
    return subscriptionDao.findSubscribersBySurveyId(surveyId);
  }

  @Transactional(readOnly = true)
  public boolean isSubscribed(UUID surveyId, String email, UUID currentUserId) {
    permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);

    Account account = findAccountByEmailOrThrow(email);
    return subscriptionDao.existsBySurveyIdAndAccountId(surveyId, account.getId());
  }

  private Account findAccountByEmailOrThrow(String email) {
    return accountDao.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Пользователь с email " + email + " не найден"));
  }
}