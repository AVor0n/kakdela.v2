package ru.hh.kakdela.v2.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.ResponseDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.security.JwtService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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

  /*@Test
  void testCheckAccessAndGetResponseWhenNormalInput() {
    String testToken = "test";
    when(responseDao.findById(responseId)).thenReturn(Optional.ofNullable(testResponse));
    when(jwtService.extractResponseId(testToken)).thenReturn(responseId);

    assertEquals(responseService.);
  }*/
}