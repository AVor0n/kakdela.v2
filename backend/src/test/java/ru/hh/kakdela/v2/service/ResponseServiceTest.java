package ru.hh.kakdela.v2.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.ResponseDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela.v2.dto.response.ResponseWithTokenDto;
import ru.hh.kakdela.v2.mapper.ResponseMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.security.JwtService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResponseServiceTest {
  @Mock
  private ResponseDao responseDao;
  @Mock
  private SurveyDao surveyDao;
  @Mock
  private AccountDao accountDao;
  @Mock
  private PermissionService permissionService;
  @Mock
  private JwtService jwtService;

  @InjectMocks
  private ResponseService responseService;

  private static UUID responseId;
  private static UUID accountId;
  private static UUID surveyId;
  private static Account testAccount;
  private static Survey testSurvey;
  private static Response testResponse;
  private static final String testToken = "test";
  private static final UUID responseIdWithNullAccountId = UUID.randomUUID();

  @BeforeAll
  static void setupAll() {
    responseId = UUID.randomUUID();
    accountId = UUID.randomUUID();
    surveyId = UUID.randomUUID();
    testAccount = Account.builder()
        .id(accountId)
        .login("test")
        .email("test@gmail.com")
        .passwordHash("$2a$10$WeEHrW1OLHk3BMFmojk94uiaO3Y62xrb.wsXRkofYdKsSsrv.jC7m")
        .registeredAt(Instant.now())
        .build();
    testSurvey = Survey.builder()
        .id(surveyId)
        .author(testAccount)
        .title("test")
        .description("test")
        .isAuthorizedOnly(false)
        .isLimitedToOneResponse(false)
        .isPublished(true)
        .isTemplate(false)
        .doNotify(false)
        .expireAt(Instant.now().plusSeconds(86400))
        .targetTimezone("Europe/Moscow")
        .createdAt(Instant.now())
        .build();
    testResponse = Response.builder()
        .id(responseId)
        .account(testAccount)
        .survey(testSurvey)
        .isCompleted(true)
        .receivedAt(Instant.now())
        .build();
  }

  // ----------------------- getById tests -----------------------
  @Test
  void testGetByIdWhenNormalInput() {
    when(responseDao.findById(responseId)).thenReturn(Optional.ofNullable(testResponse));
    when(jwtService.extractResponseId(testToken)).thenReturn(responseId);

    assertEquals(
        responseService.getById(responseId, accountId, testToken),
        ResponseMapper.responseToDto(testResponse)
    );
  }

  @Test
  void testGetByIdThrowsExceptionWhenResponseIsNull() {
    when(responseDao.findById(responseId)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.getById(responseId, accountId, testToken)
    );
    assertEquals("Ответ не найден: " + responseId, exception.getReason());
  }

  @Test
  void testGetByIdThrowsExceptionWhenAccountAndTokenIsNull() {
    Response responseWithNullAccount = getResponseWithNullAccount();
    when(responseDao.findById(responseWithNullAccount.getId()))
        .thenReturn(Optional.of(responseWithNullAccount));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.getById(responseWithNullAccount.getId(), null, null)
    );
    assertEquals("Не предоставлены учётные данные для доступа к прохождению", exception.getReason());
  }

  @Test
  void testGetByIdThrowsExceptionWhenWrongAccount() {
    when(responseDao.findById(responseId)).thenReturn(Optional.ofNullable(testResponse));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.getById(responseId, UUID.randomUUID(), testToken)
    );
    assertEquals("Вы не являетесь автором ответа", exception.getReason());
  }

  @Test
  void testGetByIdThrowsExceptionWhenWrongToken() {
    when(responseDao.findById(responseId)).thenReturn(Optional.ofNullable(testResponse));
    when(jwtService.extractResponseId("wrong_token")).thenReturn(UUID.randomUUID());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.getById(responseId, accountId, "wrong_token")
    );
    assertEquals("Вы не являетесь автором ответа", exception.getReason());
  }

  @Test
  void testGetByIdThrowsExceptionIfTryGetAnonResponse() {
    Response responseWithNullAccount = getResponseWithNullAccount();
    when(responseDao.findById(responseWithNullAccount.getId()))
        .thenReturn(Optional.of(responseWithNullAccount));
    when(jwtService.extractResponseId(testToken))
        .thenReturn(responseWithNullAccount.getId());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.getById(responseWithNullAccount.getId(), null, testToken)
    );
    assertEquals("Просмотр завершённых анонимных ответов запрещён", exception.getReason());
  }


  // ----------------------- GetCompletedBySurveyId tests -----------------------
  @Test
  void testGetCompletedBySurveyIdThrowsExceptionFromCheckAccess() {
    doThrow(ResponseStatusException.class)
        .when(permissionService)
        .checkAccess(surveyId, accountId, Permission.SurveyRole.ANALYST);

    assertThrows(
        ResponseStatusException.class,
        () -> responseService.getCompletedBySurveyId(surveyId, accountId)
    );
  }


  // ----------------------- create tests -----------------------
  @Test
  void testCreateSuccessWithAccount() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testSurvey));
    when(accountDao.findById(accountId)).thenReturn(Optional.of(testAccount));
    mockResponseSave();

    ResponseWithTokenDto result = responseService.create(surveyId, accountId);

    assertNotNull(result);
    assertEquals(responseId, result.getId());
    assertNull(result.getResponseAccessToken());
    verify(responseDao).save(any(Response.class));
  }

  @Test
  void testCreateWithoutAccount() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testSurvey));
    mockResponseSave();
    when(jwtService.generateResponseAccessToken(any(UUID.class))).thenReturn(testToken);

    ResponseWithTokenDto result = responseService.create(surveyId, null);

    assertNotNull(result);
    assertEquals(testResponse.getId(), result.getId());
    assertNotNull(result.getResponseAccessToken());
    verify(accountDao, never()).findById(any());
    verify(jwtService).generateResponseAccessToken(any(UUID.class));
  }

  @Test
  void testCreateThrowsExceptionWhenSurveyNotFound() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.create(surveyId, accountId)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Опрос не найден: " + surveyId, exception.getReason());
  }

  @Test
  void testCreateThrowsExceptionWhenSurveyNotPublished() {
    Survey unpublishedSurvey = Survey.builder()
        .id(surveyId)
        .isPublished(false)
        .build();
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(unpublishedSurvey));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.create(surveyId, accountId)
    );
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Опрос ещё не опубликован", exception.getReason());
  }

  @Test
  void testCreateWhenLimitedToOneResponseAndAlreadyExists() {
    Survey limitedSurvey = Survey.builder()
        .id(surveyId)
        .isPublished(true)
        .isLimitedToOneResponse(true)
        .build();
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(limitedSurvey));
    when(responseDao.existsBySurveyIdAndAccountId(surveyId, accountId)).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.create(surveyId, accountId)
    );
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Вы уже проходили этот опрос", exception.getReason());
  }

  @Test
  void testCreateWhenLimitedToOneResponseButAccountIsNull() {
    Survey limitedSurvey = Survey.builder()
        .id(surveyId)
        .isPublished(true)
        .isLimitedToOneResponse(true)
        .build();
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(limitedSurvey));
    mockResponseSave();
    when(jwtService.generateResponseAccessToken(any(UUID.class))).thenReturn(testToken);

    ResponseWithTokenDto result = responseService.create(surveyId, null);

    assertNotNull(result);
    assertEquals(testResponse.getId(), result.getId());
    assertEquals(testToken, result.getResponseAccessToken());
    verify(responseDao, never()).existsBySurveyIdAndAccountId(any(), any());
  }

  @Test
  void testCreateThrowsExceptionWhenAccountNotFound() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testSurvey));
    when(accountDao.findById(accountId)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.create(surveyId, accountId)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Аккаунт не найден: " + accountId, exception.getReason());
  }


  // ----------------------- complete tests -----------------------
  @Test
  void testCompleteSuccess() {
    Response incompleteResponse = Response.builder()
        .id(responseId)
        .account(testAccount)
        .survey(testSurvey)
        .isCompleted(false)
        .build();

    when(responseDao.findById(responseId)).thenReturn(Optional.of(incompleteResponse));
    when(jwtService.extractResponseId(testToken)).thenReturn(responseId);
    when(responseDao.areAllMandatoryQuestionsAnswered(responseId)).thenReturn(true);
    mockResponseUpdate();

    ResponseResponseDto result = responseService.complete(responseId, accountId, testToken);

    assertNotNull(result);
    assertEquals(responseId, result.getId());
    assertTrue(result.getIsCompleted());
    assertNotNull(result.getReceivedAt());
    verify(responseDao).update(any(Response.class));
  }

  @Test
  void testCompleteThrowsExceptionWhenNotAllMandatoryQuestionsAnswered() {
    Response incompleteResponse = Response.builder()
        .id(responseId)
        .account(testAccount)
        .survey(testSurvey)
        .isCompleted(false)
        .build();

    when(responseDao.findById(responseId)).thenReturn(Optional.of(incompleteResponse));
    when(jwtService.extractResponseId(testToken)).thenReturn(responseId);
    when(responseDao.areAllMandatoryQuestionsAnswered(responseId)).thenReturn(false);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.complete(responseId, accountId, testToken)
    );
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Не все обязательные вопросы заполнены", exception.getReason());
    verify(responseDao, never()).update(any(Response.class));
  }

  @Test
  void testCompleteThrowsExceptionWhenAlreadyCompleted() {
    Response completedResponse = Response.builder()
        .id(responseId)
        .account(testAccount)
        .survey(testSurvey)
        .isCompleted(true)
        .receivedAt(Instant.now())
        .build();

    when(responseDao.findById(responseId)).thenReturn(Optional.of(completedResponse));
    when(jwtService.extractResponseId(testToken)).thenReturn(responseId);
    when(responseDao.areAllMandatoryQuestionsAnswered(responseId)).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.complete(responseId, accountId, testToken)
    );
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Прохождение уже завершено", exception.getReason());
    verify(responseDao, never()).update(any(Response.class));
  }


  // ----------------------- вспомогательные методы -----------------------
  private Response getResponseWithNullAccount() {
    return Response.builder()
        .id(responseIdWithNullAccountId)
        .account(null)
        .survey(testSurvey)
        .isCompleted(true)
        .receivedAt(Instant.now())
        .build();
  }
  private void mockResponseUpdate() {
    doAnswer(invocation -> {
      Response response = invocation.getArgument(0);
      response.setCompleted(true);
      response.setReceivedAt(Instant.now());
      return null;
    }).when(responseDao).update(any(Response.class));
  }
  private void mockResponseSave() {
    doAnswer(invocation -> {
      Response response = invocation.getArgument(0);
      response.setId(responseId);
      return null;
    }).when(responseDao).save(any(Response.class));
  }
}