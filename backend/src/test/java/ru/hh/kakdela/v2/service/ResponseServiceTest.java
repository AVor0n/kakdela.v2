package ru.hh.kakdela.v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.ResponseDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela.v2.dto.response.ResponseWithTokenDto;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;
import ru.hh.kakdela.v2.exception.response.NotAllMandatoryQuestionsAnsweredException;
import ru.hh.kakdela.v2.mapper.ResponseMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.security.JwtService;

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
  private static UUID authorAccountId;
  private static UUID respondentAccountId;
  private static UUID surveyId;
  private static Account testAuthorAccount;
  private static Account testRespondentAccount;
  private static Survey testSurvey;
  private static Survey testLimitedSurvey;
  private static Survey testUnpublishedSurvey;
  private static Response testResponse;
  private static Response testResponseWithNullAccount;
  private static final String testToken = "test";

  @BeforeAll
  static void setupAll() {
    responseId = UUID.randomUUID();
    authorAccountId = UUID.randomUUID();
    respondentAccountId = UUID.randomUUID();
    surveyId = UUID.randomUUID();
    testAuthorAccount = Account.builder()
        .id(authorAccountId)
        .login("test1")
        .email("test2@examle.com")
        .passwordHash("$2a$10$WeEHrW1OLHk3BMFmojk94uiaO3Y62xrb.wsXRkofYdKsSsrv.jC7m")
        .registeredAt(Instant.now())
        .build();
    testRespondentAccount = Account.builder()
        .id(respondentAccountId)
        .login("test2")
        .email("test2@example.com")
        .passwordHash("$2a$10$zJJ6cDGCeBfzFxcXzOIl2untS9tw8GZP0HoPGf8XgIQKhgyg10PwC")
        .registeredAt(Instant.now())
        .build();
    testSurvey = Survey.builder()
        .id(surveyId)
        .author(testAuthorAccount)
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
        .account(testRespondentAccount)
        .survey(testSurvey)
        .isCompleted(true)
        .receivedAt(Instant.now())
        .build();

    testResponseWithNullAccount = Response.builder()
        .id(UUID.randomUUID())
        .account(null)
        .survey(testSurvey)
        .isCompleted(true)
        .receivedAt(Instant.now())
        .build();

    testLimitedSurvey = Survey.builder()
        .id(surveyId)
        .isPublished(true)
        .isLimitedToOneResponse(true)
        .build();

    testUnpublishedSurvey = Survey.builder()
        .id(surveyId)
        .isPublished(false)
        .build();
  }

  // ----------------------- getById tests -----------------------
  @Test
  void getById_normalInput_returnsCorrectDto() {
    when(responseDao.findById(responseId)).thenReturn(Optional.ofNullable(testResponse));
    when(jwtService.extractResponseId(testToken)).thenReturn(responseId);

    assertEquals(
        ResponseMapper.responseToDto(testResponse),
        responseService.getById(responseId, respondentAccountId, testToken)
    );
  }

  @Test
  void getById_responseNotFound_throwsException() {
    when(responseDao.findById(responseId)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.getById(responseId, respondentAccountId, testToken)
    );
    assertEquals("Ответ не найден: " + responseId, exception.getReason());
  }

  @Test
  void getById_accountAndTokenIsNull_throwsException() {
    Response responseWithNullAccount = testResponseWithNullAccount;
    when(responseDao.findById(responseWithNullAccount.getId()))
        .thenReturn(Optional.of(responseWithNullAccount));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.getById(responseWithNullAccount.getId(), null, null)
    );
    assertEquals("Не предоставлены учётные данные для доступа к прохождению",
        exception.getReason());
  }

  @Test
  void getById_wrongAccount_throwsException() {
    when(responseDao.findById(responseId)).thenReturn(Optional.ofNullable(testResponse));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.getById(responseId, UUID.randomUUID(), testToken)
    );
    assertEquals("Вы не являетесь автором ответа", exception.getReason());
  }

  @Test
  void getById_wrongToken_throwsException() {
    when(responseDao.findById(responseId)).thenReturn(Optional.ofNullable(testResponse));
    when(jwtService.extractResponseId("wrong_token")).thenReturn(UUID.randomUUID());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.getById(responseId, respondentAccountId, "wrong_token")
    );
    assertEquals("Вы не являетесь автором ответа", exception.getReason());
  }

  @Test
  void getById_tryGetCompletedAnonResponse_throwsException() {
    Response responseWithNullAccount = testResponseWithNullAccount;
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
  void getCompletedBySurveyId_permissionDenied_ThrowsException() {
    doThrow(ResponseStatusException.class)
        .when(permissionService)
        .checkCanReadResponses(surveyId, respondentAccountId);

    assertThrows(
        ResponseStatusException.class,
        () -> responseService.getCompletedBySurveyId(surveyId, respondentAccountId)
    );
  }


  // ----------------------- create tests -----------------------
  @Test
  void create_withAccount_success() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testSurvey));
    when(accountDao.findById(respondentAccountId)).thenReturn(Optional.of(testRespondentAccount));
    mockResponseSave();

    ResponseWithTokenDto result = responseService.create(surveyId, respondentAccountId);

    assertNotNull(result);
    assertEquals(responseId, result.getId());
    assertNull(result.getResponseAccessToken());
    verify(responseDao).save(any(Response.class));
  }

  @Test
  void create_withoutAccount_success() {
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
  void create_surveyNotFound_throwsException() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.create(surveyId, respondentAccountId)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Опрос не найден: " + surveyId, exception.getReason());
  }

  @Test
  void create_surveyNotPublished_throwsException() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testUnpublishedSurvey));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.create(surveyId, respondentAccountId)
    );
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Опрос ещё не опубликован", exception.getReason());
  }

  @Test
  void create_limitedToOneResponseAndAlreadyExists_throwsException() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testLimitedSurvey));
    when(responseDao.existsBySurveyIdAndAccountId(surveyId, respondentAccountId)).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.create(surveyId, respondentAccountId)
    );
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Вы уже проходили этот опрос", exception.getReason());
  }

  @Test
  void create_limitedToOneResponseButAccountIsNull_success() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testLimitedSurvey));
    mockResponseSave();
    when(jwtService.generateResponseAccessToken(any(UUID.class))).thenReturn(testToken);

    ResponseWithTokenDto result = responseService.create(surveyId, null);

    assertNotNull(result);
    assertEquals(testResponse.getId(), result.getId());
    assertEquals(testToken, result.getResponseAccessToken());
    verify(responseDao, never()).existsBySurveyIdAndAccountId(any(), any());
  }

  @Test
  void create_accountNotFound_throwsException() {
    when(surveyDao.findById(surveyId)).thenReturn(Optional.of(testSurvey));
    when(accountDao.findById(respondentAccountId)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.create(surveyId, respondentAccountId)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Аккаунт не найден: " + respondentAccountId, exception.getReason());
  }


  // ----------------------- complete tests -----------------------
  @Test
  void complete_normalInput_success() {
    Response incompletedResponse = Response.builder()
        .id(responseId)
        .account(testRespondentAccount)
        .survey(testSurvey)
        .isCompleted(false)
        .build();

    when(responseDao.findById(responseId)).thenReturn(Optional.of(incompletedResponse));
    when(jwtService.extractResponseId(testToken)).thenReturn(responseId);
    when(responseDao.areAllMandatoryQuestionsAnswered(responseId)).thenReturn(true);
    doNothing().when(responseDao).update(any(Response.class));

    ResponseResponseDto result = responseService.complete(responseId, respondentAccountId, testToken);

    assertNotNull(result);
    assertEquals(responseId, result.getId());
    assertTrue(result.getIsCompleted());
    assertNotNull(result.getReceivedAt());
    verify(responseDao).update(any(Response.class));
  }

  @Test
  void complete_notAllMandatoryQuestionsAnswered_throwsException() {
    Response incompleteResponse = Response.builder()
        .id(responseId)
        .account(testRespondentAccount)
        .survey(testSurvey)
        .isCompleted(false)
        .build();

    when(responseDao.findById(responseId)).thenReturn(Optional.of(incompleteResponse));
    when(jwtService.extractResponseId(testToken)).thenReturn(responseId);
    when(responseDao.areAllMandatoryQuestionsAnswered(responseId)).thenReturn(false);

    Kd2Exception exception = assertThrows(
        NotAllMandatoryQuestionsAnsweredException.class,
        () -> responseService.complete(responseId, respondentAccountId, testToken)
    );
    assertEquals(ErrorCode.NOT_ALL_MANDATORY_QUESTIONS_ANSWERED, exception.getErrorCode());
    assertEquals("Не все обязательные вопросы заполнены", exception.getMessage());
    verify(responseDao, never()).update(any(Response.class));
  }

  @Test
  void complete_alreadyCompleted_throwsException() {
    when(responseDao.findById(responseId)).thenReturn(Optional.of(testResponse));
    when(jwtService.extractResponseId(testToken)).thenReturn(responseId);
    when(responseDao.areAllMandatoryQuestionsAnswered(responseId)).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> responseService.complete(responseId, respondentAccountId, testToken)
    );
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Прохождение уже завершено", exception.getReason());
    verify(responseDao, never()).update(any(Response.class));
  }


  // ----------------------- вспомогательные методы -----------------------
  private void mockResponseSave() {
    doAnswer(invocation -> {
      Response response = invocation.getArgument(0);
      response.setId(responseId);
      return null;
    }).when(responseDao).save(any(Response.class));
  }
}
