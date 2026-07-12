package ru.hh.kakdela.v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dao.SurveyNotificationSubscriptionDao;
import ru.hh.kakdela.v2.dto.subscription.SubscriptionResponseDto;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyNotificationSubscription;

@ExtendWith(MockitoExtension.class)
class SurveyNotificationSubscriptionServiceTest {

  @Mock
  private SurveyNotificationSubscriptionDao subscriptionDao;
  @Mock
  private AccountDao accountDao;
  @Mock
  private SurveyDao surveyDao;
  @Mock
  private PermissionService permissionService;
  @Mock
  private EmailService emailService;

  @InjectMocks
  private SurveyNotificationSubscriptionService subscriptionService;

  private static UUID surveyId;
  private static UUID currentUserId;
  private static UUID accountId;
  private static Account testAccount;
  private static Survey testPublishedSurvey;
  private static Survey testUnpublishedSurvey;
  private static final String testEmail = "test@gmail.com";

  @BeforeAll
  static void setupAll() {
    surveyId = UUID.randomUUID();
    currentUserId = UUID.randomUUID();
    accountId = UUID.randomUUID();

    testAccount = Account.builder()
        .id(accountId)
        .login("test")
        .email(testEmail)
        .passwordHash("$2a$10$WeEHrW1OLHk3BMFmojk94uiaO3Y62xrb.wsXRkofYdKsSsrv.jC7m")
        .registeredAt(Instant.now())
        .build();

    testPublishedSurvey = Survey.builder()
        .id(surveyId)
        .title("test survey")
        .isPublished(true)
        .build();

    testUnpublishedSurvey = Survey.builder()
        .id(surveyId)
        .title("test survey")
        .isPublished(false)
        .build();
  }

  // ----------------------- subscribeUsers tests -----------------------

  @Test
  void subscribeUsers_exceptionFromCheckAccess_throwsException() {
    doThrow(ResponseStatusException.class)
        .when(permissionService)
        .checkAccess(surveyId, currentUserId, Permission.SurveyRole.EDITOR);

    assertThrows(
        ResponseStatusException.class,
        () -> subscriptionService.subscribeUsers(surveyId, List.of(testEmail), currentUserId)
    );
    verifyNoInteractions(surveyDao, subscriptionDao, accountDao, emailService);
  }

  @Test
  void subscribeUsers_emptyEmails_returnsEmptyDto() {
    SubscriptionResponseDto result =
        subscriptionService.subscribeUsers(surveyId, List.of(), currentUserId);

    assertEquals(List.of(), result.getSubscribedEmails());
    assertEquals(List.of(), result.getAlreadySubscribedEmails());
    assertEquals(List.of(), result.getNotFoundEmails());
    verifyNoInteractions(surveyDao, subscriptionDao, accountDao, emailService);
  }

  @Test
  void subscribeUsers_surveyNotFound_throwsException() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> subscriptionService.subscribeUsers(surveyId, List.of(testEmail), currentUserId)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Опрос не найден: " + surveyId, exception.getReason());
  }

  @Test
  void subscribeUsers_accountNotFound_addedToNotFoundEmails() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testPublishedSurvey));
    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.empty());

    SubscriptionResponseDto result = subscriptionService.subscribeUsers(
        surveyId, List.of(testEmail), currentUserId);

    assertEquals(List.of(testEmail), result.getNotFoundEmails());
    assertEquals(List.of(), result.getSubscribedEmails());
    assertEquals(List.of(), result.getAlreadySubscribedEmails());
    verifyNoInteractions(subscriptionDao, emailService);
  }

  @Test
  void subscribeUsers_alreadySubscribed_addedToAlreadySubscribedEmails() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testPublishedSurvey));
    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.of(testAccount));
    when(subscriptionDao.existsBySurveyIdAndAccountId(surveyId, accountId)).thenReturn(true);

    SubscriptionResponseDto result = subscriptionService.subscribeUsers(
        surveyId, List.of(testEmail), currentUserId);

    assertEquals(List.of(testEmail), result.getAlreadySubscribedEmails());
    assertEquals(List.of(), result.getSubscribedEmails());
    assertEquals(List.of(), result.getNotFoundEmails());
    verify(subscriptionDao, never()).addSubscription(any());
    verifyNoInteractions(emailService);
  }

  @Test
  void subscribeUsers_newSubscriptionPublishedSurvey_sendsEmailNotification() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testPublishedSurvey));
    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.of(testAccount));
    when(subscriptionDao.existsBySurveyIdAndAccountId(surveyId, accountId)).thenReturn(false);

    SubscriptionResponseDto result = subscriptionService.subscribeUsers(
        surveyId, List.of(testEmail), currentUserId);

    assertEquals(List.of(testEmail), result.getSubscribedEmails());
    assertEquals(List.of(), result.getAlreadySubscribedEmails());
    assertEquals(List.of(), result.getNotFoundEmails());

    ArgumentCaptor<SurveyNotificationSubscription> captor =
        ArgumentCaptor.forClass(SurveyNotificationSubscription.class);
    verify(subscriptionDao).addSubscription(captor.capture());
    assertEquals(testPublishedSurvey, captor.getValue().getSurvey());
    assertEquals(testAccount, captor.getValue().getAccount());

    verify(emailService).sendSurveyPublishedEmail(
        testEmail, testPublishedSurvey.getTitle(), surveyId);
  }

  @Test
  void subscribeUsers_newSubscriptionUnpublishedSurvey_doesNotSendEmailNotification() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testUnpublishedSurvey));
    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.of(testAccount));
    when(subscriptionDao.existsBySurveyIdAndAccountId(surveyId, accountId)).thenReturn(false);

    SubscriptionResponseDto result = subscriptionService.subscribeUsers(
        surveyId, List.of(testEmail), currentUserId);

    assertEquals(List.of(testEmail), result.getSubscribedEmails());
    verify(subscriptionDao).addSubscription(any());
    verifyNoInteractions(emailService);
  }

  @Test
  void subscribeUsers_mixedEmails_partitionsCorrectly() {
    String subscribedEmail = "new@gmail.com";
    String alreadySubscribedEmail = "already@gmail.com";
    String notFoundEmail = "unknown@gmail.com";

    UUID subscribedAccountId = UUID.randomUUID();
    UUID alreadySubscribedAccountId = UUID.randomUUID();

    Account subscribedAccount = Account.builder()
        .id(subscribedAccountId).email(subscribedEmail).build();
    Account alreadySubscribedAccount = Account.builder()
        .id(alreadySubscribedAccountId).email(alreadySubscribedEmail).build();

    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testPublishedSurvey));
    when(accountDao.findByEmail(subscribedEmail)).thenReturn(Optional.of(subscribedAccount));
    when(accountDao.findByEmail(alreadySubscribedEmail))
        .thenReturn(Optional.of(alreadySubscribedAccount));
    when(accountDao.findByEmail(notFoundEmail)).thenReturn(Optional.empty());
    when(subscriptionDao.existsBySurveyIdAndAccountId(surveyId, subscribedAccountId))
        .thenReturn(false);
    when(subscriptionDao.existsBySurveyIdAndAccountId(surveyId, alreadySubscribedAccountId))
        .thenReturn(true);

    SubscriptionResponseDto result = subscriptionService.subscribeUsers(
        surveyId, List.of(subscribedEmail, alreadySubscribedEmail, notFoundEmail), currentUserId);

    assertEquals(List.of(subscribedEmail), result.getSubscribedEmails());
    assertEquals(List.of(alreadySubscribedEmail), result.getAlreadySubscribedEmails());
    assertEquals(List.of(notFoundEmail), result.getNotFoundEmails());

    verify(subscriptionDao, times(1)).addSubscription(any());
    verify(emailService, times(1)).sendSurveyPublishedEmail(
        subscribedEmail, testPublishedSurvey.getTitle(), surveyId);
  }

  // ----------------------- unsubscribeUser tests -----------------------

  @Test
  void unsubscribeUser_exceptionFromCheckAccess_throwsException() {
    doThrow(ResponseStatusException.class)
        .when(permissionService)
        .checkAccess(surveyId, currentUserId, Permission.SurveyRole.EDITOR);

    assertThrows(
        ResponseStatusException.class,
        () -> subscriptionService.unsubscribeUser(surveyId, testEmail, currentUserId)
    );
    verifyNoInteractions(accountDao, subscriptionDao);
  }

  @Test
  void unsubscribeUser_accountNotFound_throwsException() {
    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> subscriptionService.unsubscribeUser(surveyId, testEmail, currentUserId)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Пользователь с email " + testEmail + " не найден", exception.getReason());
    verifyNoInteractions(subscriptionDao);
  }

  @Test
  void unsubscribeUser_subscriptionNotFound_throwsException() {
    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.of(testAccount));
    when(subscriptionDao.findBySurveyIdAndAccountId(surveyId, accountId))
        .thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> subscriptionService.unsubscribeUser(surveyId, testEmail, currentUserId)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Подписка для " + testEmail + " не найдена", exception.getReason());
    verify(subscriptionDao, never()).deleteSubscription(any());
  }

  @Test
  void unsubscribeUser_normalInput_success() {
    SurveyNotificationSubscription subscription = SurveyNotificationSubscription.builder()
        .survey(testPublishedSurvey)
        .account(testAccount)
        .build();

    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.of(testAccount));
    when(subscriptionDao.findBySurveyIdAndAccountId(surveyId, accountId))
        .thenReturn(Optional.of(subscription));

    subscriptionService.unsubscribeUser(surveyId, testEmail, currentUserId);

    verify(subscriptionDao).deleteSubscription(subscription);
  }

  // ----------------------- getSubscribers tests -----------------------

  @Test
  void getSubscribers_exceptionFromCheckAccess_throwsException() {
    doThrow(ResponseStatusException.class)
        .when(permissionService)
        .checkAccess(surveyId, currentUserId, Permission.SurveyRole.EDITOR);

    assertThrows(
        ResponseStatusException.class,
        () -> subscriptionService.getSubscribers(surveyId, currentUserId)
    );
    verifyNoInteractions(subscriptionDao);
  }

  @Test
  void getSubscribers_normalInput_returnsSubscribersList() {
    List<Account> subscribers = List.of(testAccount);
    when(subscriptionDao.findSubscribersBySurveyId(surveyId)).thenReturn(subscribers);

    List<Account> result = subscriptionService.getSubscribers(surveyId, currentUserId);

    assertEquals(subscribers, result);
  }

  @Test
  void getSubscribers_noSubscribers_returnsEmptyList() {
    when(subscriptionDao.findSubscribersBySurveyId(surveyId)).thenReturn(List.of());

    List<Account> result = subscriptionService.getSubscribers(surveyId, currentUserId);

    assertEquals(List.of(), result);
  }

  // ----------------------- isSubscribed tests -----------------------

  @Test
  void isSubscribed_exceptionFromCheckAccess_throwsException() {
    doThrow(ResponseStatusException.class)
        .when(permissionService)
        .checkAccess(surveyId, currentUserId, Permission.SurveyRole.EDITOR);

    assertThrows(
        ResponseStatusException.class,
        () -> subscriptionService.isSubscribed(surveyId, testEmail, currentUserId)
    );
    verifyNoInteractions(accountDao, subscriptionDao);
  }

  @Test
  void isSubscribed_accountNotFound_throwsException() {
    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> subscriptionService.isSubscribed(surveyId, testEmail, currentUserId)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Пользователь с email " + testEmail + " не найден", exception.getReason());
    verifyNoInteractions(subscriptionDao);
  }

  @Test
  void isSubscribed_accountIsSubscribed_returnsTrue() {
    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.of(testAccount));
    when(subscriptionDao.existsBySurveyIdAndAccountId(surveyId, accountId)).thenReturn(true);

    boolean result = subscriptionService.isSubscribed(surveyId, testEmail, currentUserId);

    assertTrue(result);
  }

  @Test
  void isSubscribed_accountIsNotSubscribed_returnsFalse() {
    when(accountDao.findByEmail(testEmail)).thenReturn(Optional.of(testAccount));
    when(subscriptionDao.existsBySurveyIdAndAccountId(surveyId, accountId)).thenReturn(false);

    boolean result = subscriptionService.isSubscribed(surveyId, testEmail, currentUserId);

    assertFalse(result);
  }
}
