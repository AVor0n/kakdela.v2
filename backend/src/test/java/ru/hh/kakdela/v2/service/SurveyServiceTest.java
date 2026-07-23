package ru.hh.kakdela.v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyWithUserRoleDto;
import ru.hh.kakdela.v2.mapper.AnswerOptionMapper;
import ru.hh.kakdela.v2.mapper.ClosingPageMapper;
import ru.hh.kakdela.v2.mapper.QuestionMapper;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.mapper.SurveyPageMapper;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.util.SurveyServiceTestUtil;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

  @Mock
  private SurveyDao surveyDao;
  @Mock
  private AccountDao accountDao;
  @Mock
  private PermissionService permissionService;
  @Mock
  private NotificationService notificationService;
  @Mock
  private ObjectStorageService objectStorageService;

  private SurveyService surveyService;

  private static final Survey fullSurvey =
      SurveyServiceTestUtil.getFullSurvey(true, false);
  private static final SurveyResponseDto fullSurveyResponseDto =
      SurveyServiceTestUtil.getResponseDtoOfFullSurvey(true, false);
  private static final SurveyShortResponseWithPermissionDto fullSurveyShortResponseDto =
      SurveyServiceTestUtil.getShortResponseDtoOfFullSurvey(false);

  private static final Survey fullSurveyWithoutClosingPage =
      SurveyServiceTestUtil.getFullSurvey(false, false);

  private static final Survey fullSurveyClone =
      SurveyServiceTestUtil.getFullSurvey(true, true);
  private static final SurveyResponseDto fullSurveyCloneResponseDto =
      SurveyServiceTestUtil.getResponseDtoOfFullSurvey(true, true);

  private static final Survey fullSurveyCloneWithoutClosingPage =
      SurveyServiceTestUtil.getFullSurvey(false, true);
  private static final SurveyResponseDto fullSurveyCloneWithoutClosingPageResponseDto =
      SurveyServiceTestUtil.getResponseDtoOfFullSurvey(false, true);

  private static final Survey plainSurveyUnpublished =
      SurveyServiceTestUtil.getPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestUtil.expireAt,
          "Asia/Yekaterinburg");
  private static final SurveyResponseDto plainSurveyUnpublishedResponseDto =
      SurveyServiceTestUtil.getResponseDtoOfPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestUtil.expireAt,
          SurveyServiceTestUtil.expireAtAtYekaterinburgTimezone,
          "Asia/Yekaterinburg");
  private static final SurveyShortResponseWithPermissionDto plainSurveyUnpublishedShortResponseDto =
      SurveyServiceTestUtil.getShortResponseDtoOfPlainSurvey(false, false);
  private static final SurveyCreateDto plainSurveyUnpublishedCreateDto =
      SurveyServiceTestUtil.getCreateDtoForPlainSurvey(
          false,
          SurveyServiceTestUtil.expireAtAtYekaterinburgTimezone,
          "Asia/Yekaterinburg");
  private static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoNoChanges =
      SurveyServiceTestUtil.getUpdateDtoForPlainSurvey(
          null,
          null,
          null,
          null,
          null);
  private static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoPublished =
      SurveyServiceTestUtil.getUpdateDtoForPlainSurvey(
          null,
          null,
          true,
          null,
          null);

  private static final Survey plainSurveyUnpublishedNoExpireAt =
      SurveyServiceTestUtil.getPlainSurvey(
          false,
          false,
          false,
          null,
          "Europe/Moscow");
  private static final SurveyResponseDto plainSurveyUnpublishedNoExpireAtResponseDto =
      SurveyServiceTestUtil.getResponseDtoOfPlainSurvey(
          false,
          false,
          false,
          null,
          null,
          "Europe/Moscow");
  private static final SurveyCreateDto plainSurveyUnpublishedNoExpireAtCreateDto =
      SurveyServiceTestUtil.getCreateDtoForPlainSurvey(
          false,
          null,
          null);

  private static final Survey plainSurveyUnpublishedAnotherExpireAt =
      SurveyServiceTestUtil.getPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestUtil.anotherExpireAt,
          "Asia/Yekaterinburg");
  private static final SurveyResponseDto plainSurveyUnpublishedAnotherExpireAtResponseDto =
      SurveyServiceTestUtil.getResponseDtoOfPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestUtil.anotherExpireAt,
          SurveyServiceTestUtil.anotherExpireAtAtYekaterinburgTimezone,
          "Asia/Yekaterinburg");
  private static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoAnotherExpireAt =
      SurveyServiceTestUtil.getUpdateDtoForPlainSurvey(
          null,
          null,
          null,
          SurveyServiceTestUtil.anotherExpireAtAtYekaterinburgTimezone,
          null);

  private static final Survey plainSurveyUnpublishedAnotherTargetTimezone =
      SurveyServiceTestUtil.getPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestUtil.expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka,
          "Asia/Kamchatka");
  private static final SurveyResponseDto plainSurveyUnpublishedAnotherTargetTimezoneResponseDto =
      SurveyServiceTestUtil.getResponseDtoOfPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestUtil.expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka,
          SurveyServiceTestUtil.expireAtAtYekaterinburgTimezone, "Asia/Kamchatka");
  private static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoAnotherTargetTimezone =
      SurveyServiceTestUtil.getUpdateDtoForPlainSurvey(
          null,
          null,
          null,
          null,
          "Asia/Kamchatka");

  private static final Survey plainSurveyUnpublishedOtherValuesExceptIsPublished =
      SurveyServiceTestUtil.getPlainSurvey(
          true,
          true,
          false,
          SurveyServiceTestUtil.anotherExpireAt,
          "Asia/Kamchatka");
  private static final SurveyResponseDto plainSurveyOtherValuesExceptIsPublishedResponseDto =
      SurveyServiceTestUtil.getResponseDtoOfPlainSurvey(
          true,
          true,
          false,
          SurveyServiceTestUtil.anotherExpireAt,
          SurveyServiceTestUtil.anotherExpireAtAtKamchatkaTimezone,
          "Asia/Kamchatka");
  private static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoOtherValuesExceptIsPublished =
      SurveyServiceTestUtil.getUpdateDtoForPlainSurvey(
          true,
          true,
          null,
          SurveyServiceTestUtil.anotherExpireAtAtKamchatkaTimezone,
          "Asia/Kamchatka");

  private static final String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

  @BeforeEach
  void setUp() {
    AnswerOptionMapper answerOptionMapper = new AnswerOptionMapper(objectStorageService);
    QuestionMapper questionMapper = new QuestionMapper(objectStorageService, answerOptionMapper);
    SurveyPageMapper surveyPageMapper = new SurveyPageMapper(questionMapper);
    ClosingPageMapper closingPageMapper = new ClosingPageMapper(objectStorageService);
    SurveyMapper surveyMapper = new SurveyMapper(surveyPageMapper, closingPageMapper);
    surveyService = new SurveyService(
        surveyDao,
        accountDao,
        permissionService,
        notificationService,
        objectStorageService,
        surveyMapper
    );
  }

  // getById

  @Test
  void getById_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.getById(SurveyServiceTestUtil.fullSurveyId)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + SurveyServiceTestUtil.fullSurveyId + "\"",
        ex.getMessage()
    );
  }

  @Test
  void getById_surveyFound_returnCorrectDto() throws MalformedURLException {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.of(fullSurvey));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    SurveyResponseDto result = surveyService.getById(SurveyServiceTestUtil.fullSurveyId);
    assertEquals(fullSurveyResponseDto, result);
  }

  // getMySurveys

  @Test
  void getMySurveys_surveysNotFound_returnEmptyListOfDto() {
    Mockito.when(permissionService.getAccessibleSurveys(SurveyServiceTestUtil.account1Id))
        .thenReturn(Collections.emptyList());

    List<SurveyShortResponseWithPermissionDto> result = surveyService.getMySurveys(SurveyServiceTestUtil.account1Id);
    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void getMySurveys_surveysFound_returnCorrectListOfDto() {
    Mockito.when(permissionService.getAccessibleSurveys(SurveyServiceTestUtil.account1Id))
        .thenReturn(List.of(
            new SurveyWithUserRoleDto(SurveyServiceTestUtil.getFullSurvey(true, false), Permission.SurveyRole.AUTHOR),
            new SurveyWithUserRoleDto(plainSurveyUnpublished, Permission.SurveyRole.AUTHOR)
        ));

    List<SurveyShortResponseWithPermissionDto> result = surveyService.getMySurveys(SurveyServiceTestUtil.account1Id);
    assertEquals(List.of(fullSurveyShortResponseDto, plainSurveyUnpublishedShortResponseDto), result);
  }

  // create

  @Test
  void create_accountNotFound_throwsException() {
    Mockito.when(accountDao.findById(SurveyServiceTestUtil.account1Id))
        .thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class,
        () -> surveyService.create(SurveyServiceTestUtil.account1Id, plainSurveyUnpublishedCreateDto));
  }

  @Test
  void create_expireAtSet_createCorrectEntity() {
    Mockito.when(accountDao.findById(SurveyServiceTestUtil.account1Id))
        .thenReturn(Optional.of(SurveyServiceTestUtil.account1));
    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      // Проверка автоматической генерации ID
      assertNotEquals(null, survey.getId());
      assertTrue(survey.getId().toString().matches(uuidRegex));

      survey.setId(SurveyServiceTestUtil.plainSurveyId);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      // Проверка автора
      assertEquals(SurveyServiceTestUtil.account1, survey.getAuthor());
      // Созданный опрос не должен быть шаблоном
      assertFalse(survey.isTemplate());
      // Проверка дедлайна прохождения
      assertEquals(plainSurveyUnpublished.getExpireAt(), survey.getExpireAt());
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000);

      assertEquals(plainSurveyUnpublished, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.create(SurveyServiceTestUtil.account1Id, plainSurveyUnpublishedCreateDto);
    assertEquals(plainSurveyUnpublishedResponseDto, result);
  }

  @Test
  void create_noExpireAtSet_createCorrectEntity() {
    Mockito.when(accountDao.findById(SurveyServiceTestUtil.account1Id))
        .thenReturn(Optional.of(SurveyServiceTestUtil.account1));
    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      // Проверка автоматической генерации ID
      assertNotEquals(null, survey.getId());
      assertTrue(survey.getId().toString().matches(uuidRegex));

      survey.setId(SurveyServiceTestUtil.plainSurveyId);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      // Проверка автора
      assertEquals(SurveyServiceTestUtil.account1, survey.getAuthor());
      // Созданный опрос не должен быть шаблоном
      assertFalse(survey.isTemplate());
      // Проверка дедлайна прохождения
      assertNull(survey.getExpireAt());
      // Часовой пояс должен быть установлен в значение по умолчанию
      assertEquals("Europe/Moscow", survey.getTargetTimezone());

      assertEquals(plainSurveyUnpublishedNoExpireAt, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.create(SurveyServiceTestUtil.account1Id, plainSurveyUnpublishedNoExpireAtCreateDto);
    assertEquals(plainSurveyUnpublishedNoExpireAtResponseDto, result);
  }

  // update

  @Test
  void update_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.plainSurveyId))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.update(SurveyServiceTestUtil.plainSurveyId, plainSurveyUnpublishedUpdateDtoNoChanges, SurveyServiceTestUtil.account1Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + SurveyServiceTestUtil.plainSurveyId + "\"",
        ex.getMessage()
    );

    Mockito.verify(permissionService)
        .checkCanEdit(SurveyServiceTestUtil.plainSurveyId, SurveyServiceTestUtil.account1Id);
  }

  @Test
  void update_surveyFound_checkPermissions() {
    // Подготовка данных
    Survey surveyToUpdate =
        SurveyServiceTestUtil.getPlainSurvey(
            false,
            false,
            false,
            SurveyServiceTestUtil.expireAt,
            "Asia/Yekaterinburg");

    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    surveyService.update(
        SurveyServiceTestUtil.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoNoChanges,
        SurveyServiceTestUtil.account1Id
    );

    Mockito.verify(permissionService)
        .checkCanEdit(SurveyServiceTestUtil.plainSurveyId, SurveyServiceTestUtil.account1Id);
  }

  @Test
  void update_nothingChanged_returnSameSurveyDto() {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.plainSurveyId))
        .thenReturn(Optional.of(plainSurveyUnpublished));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertEquals(plainSurveyUnpublished, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        SurveyServiceTestUtil.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoNoChanges,
        SurveyServiceTestUtil.account1Id
    );
    assertEquals(plainSurveyUnpublishedResponseDto, result);

    Mockito.verify(notificationService, never())
        .sendSurveyPublishedNotifications(SurveyServiceTestUtil.plainSurveyId);
  }

  @Test
  void update_isPublishedChangedToTrueButSurveyAlreadyPublished_doNotSendNotificationToSurveyParticipants() {
    // Подготовка данных
    Survey surveyToUpdate =
        SurveyServiceTestUtil.getPlainSurvey(
            false,
            false,
            true,
            SurveyServiceTestUtil.expireAt,
            "Asia/Yekaterinburg");

    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    surveyService.update(
        SurveyServiceTestUtil.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoPublished,
        SurveyServiceTestUtil.account1Id
    );

    Mockito.verify(notificationService, never())
        .sendSurveyPublishedNotifications(SurveyServiceTestUtil.plainSurveyId);
  }

  @Test
  void update_isPublishedChangedToTrue_sendNotificationToSurveyParticipants() {
    // Подготовка данных
    Survey surveyToUpdate = SurveyServiceTestUtil.getPlainSurvey(
        false,
        false,
        false,
        SurveyServiceTestUtil.expireAt,
        "Asia/Yekaterinburg");

    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    surveyService.update(
        SurveyServiceTestUtil.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoPublished,
        SurveyServiceTestUtil.account1Id
    );

    Mockito.verify(notificationService)
        .sendSurveyPublishedNotifications(SurveyServiceTestUtil.plainSurveyId);
  }

  @Test
  void update_expireAtChanged_convertNewExpireAtToUtcCorrectly() {
    // Подготовка данных
    Survey surveyToUpdate = SurveyServiceTestUtil.getPlainSurvey(
        false,
        false,
        false,
        SurveyServiceTestUtil.expireAt,
        "Asia/Yekaterinburg");

    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      // Проверка дедлайна прохождения
      assertEquals(plainSurveyUnpublishedAnotherExpireAt.getExpireAt(), survey.getExpireAt());
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000);

      assertEquals(plainSurveyUnpublishedAnotherExpireAt, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        SurveyServiceTestUtil.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoAnotherExpireAt,
        SurveyServiceTestUtil.account1Id
    );

    // Дедлайн прохождения должен быть правильно конвертирован в указанный часовой пояс
    assertEquals(plainSurveyUnpublishedAnotherExpireAtResponseDto.getExpireAtAtTargetTimezone(),
        result.getExpireAtAtTargetTimezone());

    assertEquals(plainSurveyUnpublishedAnotherExpireAtResponseDto, result);
  }

  // В тесте ниже дедлайн прохождения изменяется из-за смены часового пояса.
  // Его новое значение (в UTC) должно иметь такое же значение при конвертации в новый часовой пояс,
  // как старое имело при конвертации в старый часовой пояс

  @Test
  void update_targetTimezoneChanged_updateExpireAt() {
    // Data prepare
    Survey surveyToUpdate = SurveyServiceTestUtil.getPlainSurvey(
        false,
        false,
        false,
        SurveyServiceTestUtil.expireAt,
        "Asia/Yekaterinburg");

    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      // Проверка дедлайна прохождения
      assertEquals(plainSurveyUnpublishedAnotherTargetTimezone.getExpireAt(),
          survey.getExpireAt());
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000);

      assertEquals(plainSurveyUnpublishedAnotherTargetTimezone, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        SurveyServiceTestUtil.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoAnotherTargetTimezone,
        SurveyServiceTestUtil.account1Id
    );

    // Часовой пояс был изменён с Asia/Yekateringburg на Asia/Kamchatka,
    // но значение времени при конвертации в новый часовой пояс должна остаться тем же,
    // что и старое значение времени при конвертации в старый часовой пояс
    assertEquals(plainSurveyUnpublishedAnotherTargetTimezoneResponseDto.getExpireAtAtTargetTimezone(),
        result.getExpireAtAtTargetTimezone());

    assertEquals(plainSurveyUnpublishedAnotherTargetTimezoneResponseDto, result);
  }

  @Test
  void update_everythingExceptIsPublishedChanged_updateEntityCorrectly() {
    // Data prepare
    Survey surveyToUpdate = SurveyServiceTestUtil.getPlainSurvey(
        false,
        false,
        false,
        SurveyServiceTestUtil.expireAt,
        "Asia/Yekaterinburg");

    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertEquals(plainSurveyUnpublishedOtherValuesExceptIsPublished, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        SurveyServiceTestUtil.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoOtherValuesExceptIsPublished,
        SurveyServiceTestUtil.account1Id
    );
    assertEquals(plainSurveyOtherValuesExceptIsPublishedResponseDto, result);

    Mockito.verify(notificationService, never())
        .sendSurveyPublishedNotifications(SurveyServiceTestUtil.plainSurveyId);
  }

  // clone

  @Test
  void clone_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.clone(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account2Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + SurveyServiceTestUtil.fullSurveyId + "\"",
        ex.getMessage()
    );
  }

  @Test
  void clone_accountNotFound_throwException() {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.of(SurveyServiceTestUtil.getFullSurvey(true, false)));
    Mockito.when(accountDao.findById(SurveyServiceTestUtil.account2Id))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.clone(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account2Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Аккаунт не найден: " + SurveyServiceTestUtil.account2Id + "\"",
        ex.getMessage()
    );
  }

  @Test
  void clone_surveyFound_checkPermissions() throws MalformedURLException {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.of(fullSurvey));
    Mockito.when(accountDao.findById(SurveyServiceTestUtil.account2Id))
        .thenReturn(Optional.of(SurveyServiceTestUtil.account2));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    surveyService.clone(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account2Id);

    Mockito.verify(permissionService)
        .checkCanEdit(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account2Id);
  }

  @Test
  void clone_surveyWithClosingPage_createCorrectCloneEntity() throws MalformedURLException {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.of(fullSurvey));
    Mockito.when(accountDao.findById(SurveyServiceTestUtil.account2Id))
        .thenReturn(Optional.of(SurveyServiceTestUtil.account2));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      // Проверка автоматической генерации ID опроса
      assertNotEquals(null, survey.getId());
      assertTrue(survey.getId().toString().matches(uuidRegex));

      survey.setId(SurveyServiceTestUtil.fullSurveyCloneId);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      // Копия опроса должна иметь соответствующий префикс в названии
      assertTrue(survey.getTitle().matches("^Копия — .*$"));
      // Копия опроса не должна быть опубликована
      assertFalse(survey.isPublished());
      // Копия опроса не должна иметь ответов
      assertEquals(Collections.emptyList(), survey.getResponses());
      // Копия опроса не должна настроенных прав
      assertEquals(Collections.emptyList(), survey.getPermissions());

      SurveyPage page = survey.getPages().getFirst();

      // Проверка автоматической генерации ID страницы
      assertNotEquals(null, page.getId());
      assertTrue(page.getId().toString().matches(uuidRegex));

      page.setId(SurveyServiceTestUtil.page1CloneId);

      // Проверка автоматической генерации ID вопроса
      assertNotEquals(null, page.getQuestions().getFirst().getId());
      assertTrue(page.getQuestions().getFirst().getId().toString().matches(uuidRegex));

      // Проверка автоматической генерации ключа для картинки вопроса
      assertNotEquals(null, page.getQuestions().getFirst().getAttachmentObjectKey());
      assertTrue(page.getQuestions().getFirst().getAttachmentObjectKey()
          .matches("^questions/" + page.getQuestions().getFirst().getId() +
              "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"));

      page.getQuestions().getFirst().setAttachmentObjectKey("attachmentObjectKey");
      page.getQuestions().getFirst().setId(SurveyServiceTestUtil.question1CloneId);

      Question question2 = page.getQuestions().get(1);
      page.getQuestions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question2.setId(SurveyServiceTestUtil.question2CloneId);

      // Проверка автоматической генерации ID варианта ответа
      assertNotEquals(null, question2.getAnswerOptions().getFirst().getId());
      assertTrue(question2.getAnswerOptions().getFirst().getId().toString().matches(uuidRegex));

      // Проверка автоматической генерации ключа для картинки варианта ответа
      assertNotEquals(null, question2.getAnswerOptions().getFirst().getAttachmentObjectKey());
      assertTrue(question2.getAnswerOptions().getFirst().getAttachmentObjectKey()
          .matches("^answer-options/" + question2.getAnswerOptions().getFirst().getId() +
              "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"));

      question2.getAnswerOptions().get(0).setAttachmentObjectKey("attachmentObjectKey");
      question2.getAnswerOptions().get(0).setId(SurveyServiceTestUtil.answerOption1OfQuestion2CloneId);
      question2.getAnswerOptions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question2.getAnswerOptions().get(1).setId(SurveyServiceTestUtil.answerOption2OfQuestion2CloneId);

      Question question3 = page.getQuestions().get(2);
      page.getQuestions().get(2).setAttachmentObjectKey("attachmentObjectKey");
      question3.setId(SurveyServiceTestUtil.question3CloneId);
      question3.getAnswerOptions().get(0).setAttachmentObjectKey("attachmentObjectKey");
      question3.getAnswerOptions().get(0).setId(SurveyServiceTestUtil.answerOption1OfQuestion3CloneId);
      question3.getAnswerOptions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question3.getAnswerOptions().get(1).setId(SurveyServiceTestUtil.answerOption2OfQuestion3CloneId);

      // Проверка автоматической генерации ID завершающей страницы
      assertNotEquals(null, survey.getClosingPage().getId());
      assertTrue(survey.getClosingPage().getId().toString().matches(uuidRegex));

      survey.getClosingPage().setAttachmentObjectKey("attachmentObjectKey");
      survey.getClosingPage().setId(SurveyServiceTestUtil.closingPage1CloneId);

      assertEquals(fullSurveyClone, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.clone(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account2Id);
    assertEquals(fullSurveyCloneResponseDto, result);

    Mockito.verify(objectStorageService, times(7)).copyObject(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void clone_surveyWithoutClosingPage_createCorrectCloneEntity() throws MalformedURLException {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.of(fullSurveyWithoutClosingPage));
    Mockito.when(accountDao.findById(SurveyServiceTestUtil.account2Id))
        .thenReturn(Optional.of(SurveyServiceTestUtil.account2));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      // Проверка автоматической генерации ID опроса
      assertNotEquals(null, survey.getId());
      assertTrue(survey.getId().toString().matches(uuidRegex));

      survey.setId(SurveyServiceTestUtil.fullSurveyCloneId);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      // Копия опроса должна иметь соответствующий префикс в названии
      assertTrue(survey.getTitle().matches("^Копия — .*$"));
      // Копия опроса не должна быть опубликована
      assertFalse(survey.isPublished());
      // Копия опроса не должна иметь ответов
      assertEquals(Collections.emptyList(), survey.getResponses());
      // Копия опроса не должна настроенных прав
      assertEquals(Collections.emptyList(), survey.getPermissions());
      // Копия опроса без завершающей страницы не должна иметь её
      assertNull(survey.getClosingPage());

      SurveyPage page = survey.getPages().getFirst();

      // Проверка автоматической генерации ID страницы
      assertNotEquals(null, page.getId());
      assertTrue(page.getId().toString().matches(uuidRegex));

      page.setId(SurveyServiceTestUtil.page1CloneId);

      // Проверка автоматической генерации ID вопроса
      assertNotEquals(null, page.getQuestions().getFirst().getId());
      assertTrue(page.getQuestions().getFirst().getId().toString().matches(uuidRegex));

      // Проверка автоматической генерации ключа для картинки вопроса
      assertNotEquals(null, page.getQuestions().getFirst().getAttachmentObjectKey());
      assertTrue(page.getQuestions().getFirst().getAttachmentObjectKey()
          .matches("^questions/" + page.getQuestions().getFirst().getId() +
              "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"));

      page.getQuestions().getFirst().setAttachmentObjectKey("attachmentObjectKey");
      page.getQuestions().getFirst().setId(SurveyServiceTestUtil.question1CloneId);

      Question question2 = page.getQuestions().get(1);
      page.getQuestions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question2.setId(SurveyServiceTestUtil.question2CloneId);

      // Проверка автоматической генерации ID варианта ответа
      assertNotEquals(null, question2.getAnswerOptions().getFirst().getId());
      assertTrue(question2.getAnswerOptions().getFirst().getId().toString().matches(uuidRegex));

      // Проверка автоматической генерации ключа для картинки варианта ответа
      assertNotEquals(null, question2.getAnswerOptions().getFirst().getAttachmentObjectKey());
      assertTrue(question2.getAnswerOptions().getFirst().getAttachmentObjectKey()
          .matches("^answer-options/" + question2.getAnswerOptions().getFirst().getId() +
              "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"));

      question2.getAnswerOptions().get(0).setAttachmentObjectKey("attachmentObjectKey");
      question2.getAnswerOptions().get(0).setId(SurveyServiceTestUtil.answerOption1OfQuestion2CloneId);
      question2.getAnswerOptions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question2.getAnswerOptions().get(1).setId(SurveyServiceTestUtil.answerOption2OfQuestion2CloneId);

      Question question3 = page.getQuestions().get(2);
      page.getQuestions().get(2).setAttachmentObjectKey("attachmentObjectKey");
      question3.setId(SurveyServiceTestUtil.question3CloneId);
      question3.getAnswerOptions().get(0).setAttachmentObjectKey("attachmentObjectKey");
      question3.getAnswerOptions().get(0).setId(SurveyServiceTestUtil.answerOption1OfQuestion3CloneId);
      question3.getAnswerOptions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question3.getAnswerOptions().get(1).setId(SurveyServiceTestUtil.answerOption2OfQuestion3CloneId);

      assertEquals(fullSurveyCloneWithoutClosingPage, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.clone(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account2Id);
    assertEquals(fullSurveyCloneWithoutClosingPageResponseDto, result);

    Mockito.verify(objectStorageService, times(7)).copyObject(Mockito.anyString(), Mockito.anyString());
  }

  // delete

  @Test
  void delete_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.delete(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account1Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + SurveyServiceTestUtil.fullSurveyId + "\"",
        ex.getMessage()
    );
  }

  @Test
  void delete_surveyFound_checkPermissions() {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.of(fullSurvey));

    surveyService.delete(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account1Id);

    Mockito.verify(permissionService)
        .checkOwnership(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account1Id);
  }

  @Test
  void delete_deletionPermitted_callDaoDeleteMethod() {
    Mockito.when(surveyDao.findById(SurveyServiceTestUtil.fullSurveyId))
        .thenReturn(Optional.of(fullSurvey));

    surveyService.delete(SurveyServiceTestUtil.fullSurveyId, SurveyServiceTestUtil.account1Id);

    Mockito.verify(surveyDao).delete(fullSurvey);
  }

}
