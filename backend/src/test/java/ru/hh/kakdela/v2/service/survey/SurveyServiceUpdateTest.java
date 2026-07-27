package ru.hh.kakdela.v2.service.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants;

public class SurveyServiceUpdateTest extends SurveyServiceTestBase {

  @Test
  void update_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(SurveyServiceTestConstants.plainSurveyId))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.update(SurveyServiceTestConstants.plainSurveyId, plainSurveyUnpublishedUpdateDtoNoChanges, SurveyServiceTestConstants.account1Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + SurveyServiceTestConstants.plainSurveyId + "\"",
        ex.getMessage()
    );

    Mockito.verify(permissionService)
        .checkCanEdit(SurveyServiceTestConstants.plainSurveyId, SurveyServiceTestConstants.account1Id);
  }

  @Test
  void update_surveyFound_checkPermissions() {
    Survey surveyToUpdate = SurveyServiceTestBase.getFreshPlainSurveyUnpublished();

    Mockito.when(surveyDao.findById(SurveyServiceTestConstants.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    surveyService.update(
        SurveyServiceTestConstants.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoNoChanges,
        SurveyServiceTestConstants.account1Id
    );

    Mockito.verify(permissionService)
        .checkCanEdit(SurveyServiceTestConstants.plainSurveyId, SurveyServiceTestConstants.account1Id);
  }

  @Test
  void update_nothingChanged_returnSameSurveyDto() {
    Survey surveyToUpdate = SurveyServiceTestBase.getFreshPlainSurveyUnpublished();

    Mockito.when(surveyDao.findById(SurveyServiceTestConstants.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertEquals(plainSurveyUnpublished, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        SurveyServiceTestConstants.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoNoChanges,
        SurveyServiceTestConstants.account1Id
    );
    assertEquals(plainSurveyUnpublishedResponseDto, result);

    Mockito.verify(notificationService, never())
        .sendSurveyPublishedNotifications(SurveyServiceTestConstants.plainSurveyId);
  }

  @Test
  void update_isPublishedChangedToTrueButSurveyAlreadyPublished_doNotSendNotificationToSurveyParticipants() {
    Survey surveyToUpdate = SurveyServiceTestBase.getFreshPlainSurveyPublished();

    Mockito.when(surveyDao.findById(SurveyServiceTestConstants.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    surveyService.update(
        SurveyServiceTestConstants.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoPublished,
        SurveyServiceTestConstants.account1Id
    );

    Mockito.verify(notificationService, never())
        .sendSurveyPublishedNotifications(SurveyServiceTestConstants.plainSurveyId);
  }

  @Test
  void update_isPublishedChangedToTrue_sendNotificationToSurveyParticipants() {
    Survey surveyToUpdate = SurveyServiceTestBase.getFreshPlainSurveyUnpublished();

    Mockito.when(surveyDao.findById(SurveyServiceTestConstants.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    surveyService.update(
        SurveyServiceTestConstants.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoPublished,
        SurveyServiceTestConstants.account1Id
    );

    Mockito.verify(notificationService)
        .sendSurveyPublishedNotifications(SurveyServiceTestConstants.plainSurveyId);
  }

  @Test
  void update_expireAtChanged_convertNewExpireAtToUtcCorrectly() {
    Survey surveyToUpdate = SurveyServiceTestBase.getFreshPlainSurveyUnpublished();

    Mockito.when(surveyDao.findById(SurveyServiceTestConstants.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertEquals(plainSurveyUnpublishedAnotherExpireAt.getExpireAt(), survey.getExpireAt(),
          "Дедлайн прохождения должен быть правильно конвертирован в UTC");
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000,
          "Дедлайн прохождения должен иметь обрезанные миллисекунды и наносекунды");

      assertEquals(plainSurveyUnpublishedAnotherExpireAt, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        SurveyServiceTestConstants.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoAnotherExpireAt,
        SurveyServiceTestConstants.account1Id
    );

    assertEquals(plainSurveyUnpublishedAnotherExpireAtResponseDto.getExpireAtAtTargetTimezone(),
        result.getExpireAtAtTargetTimezone(),
        "Дедлайн прохождения должен быть правильно конвертирован в указанный часовой пояс");

    assertEquals(plainSurveyUnpublishedAnotherExpireAtResponseDto, result);
  }

  // В тесте ниже дедлайн прохождения изменяется из-за смены часового пояса.
  // Его новое значение (в UTC) должно иметь такое же значение при конвертации в новый часовой пояс,
  // как старое имело при конвертации в старый часовой пояс

  @Test
  void update_targetTimezoneChanged_updateExpireAt() {
    Survey surveyToUpdate = SurveyServiceTestBase.getFreshPlainSurveyUnpublished();

    Mockito.when(surveyDao.findById(SurveyServiceTestConstants.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertEquals(plainSurveyUnpublishedAnotherTargetTimezone.getExpireAt(),
          survey.getExpireAt(),
          "Дедлайн прохождения должен быть правильно конвертирован в UTC "
              + "в соответствие с новым часовым поясом");
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000,
          "Дедлайн прохождения должен иметь обрезанные миллисекунды и наносекунды");

      assertEquals(plainSurveyUnpublishedAnotherTargetTimezone, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        SurveyServiceTestConstants.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoAnotherTargetTimezone,
        SurveyServiceTestConstants.account1Id
    );

    assertEquals(plainSurveyUnpublishedAnotherTargetTimezoneResponseDto.getExpireAtAtTargetTimezone(),
        result.getExpireAtAtTargetTimezone(),
        "Часовой пояс был изменён с Asia/Yekateringburg на Asia/Kamchatka,"
        + " но значение времени при конвертации в новый часовой пояс должно остаться тем же,"
        + " что и старое значение времени при конвертации в старый часовой пояс");

    assertEquals(plainSurveyUnpublishedAnotherTargetTimezoneResponseDto, result);
  }

  @Test
  void update_everythingExceptIsPublishedChanged_updateEntityCorrectly() {
    Survey surveyToUpdate = SurveyServiceTestBase.getFreshPlainSurveyUnpublished();

    Mockito.when(surveyDao.findById(SurveyServiceTestConstants.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertEquals(plainSurveyUnpublishedOtherValuesExceptIsPublished, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        SurveyServiceTestConstants.plainSurveyId,
        plainSurveyUnpublishedUpdateDtoOtherValuesExceptIsPublished,
        SurveyServiceTestConstants.account1Id
    );
    assertEquals(plainSurveyOtherValuesExceptIsPublishedResponseDto, result);

    Mockito.verify(notificationService, never())
        .sendSurveyPublishedNotifications(SurveyServiceTestConstants.plainSurveyId);
  }
}
