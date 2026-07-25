package ru.hh.kakdela.v2.service.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestEntity;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestIdAndTime;

public class SurveyServiceCreateTest extends SurveyServiceTest {

  @Test
  void create_accountNotFound_throwsException() {
    Mockito.when(accountDao.findById(SurveyServiceTestIdAndTime.account1Id))
        .thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class,
        () -> surveyService.create(SurveyServiceTestIdAndTime.account1Id, plainSurveyUnpublishedCreateDto));
  }

  @Test
  void create_expireAtSet_createCorrectEntity() {
    Mockito.when(accountDao.findById(SurveyServiceTestIdAndTime.account1Id))
        .thenReturn(Optional.of(SurveyServiceTestEntity.account1));
    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertNotNull(survey.getId(), "ID опроса не был заполнен");
      assertTrue(survey.getId().toString().matches(uuidRegex), "ID не является UUID");

      survey.setId(SurveyServiceTestIdAndTime.plainSurveyId);

      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()),
          "Установленное время создания оказалось в будущем или было слишком давно");
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000,
          "Время создания должно иметь обрезанные миллисекунды и наносекунды");

      survey.setCreatedAt(null);

      assertEquals(SurveyServiceTestEntity.account1, survey.getAuthor(),
          "Автор опроса должен быть установлен правильно");
      assertFalse(survey.isTemplate(),
          "Созданный опрос не должен быть шаблоном");
      assertEquals(plainSurveyUnpublished.getExpireAt(), survey.getExpireAt(),
          "Дедлайн прохождения должен быть правильно конвертирован в UTC");
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000,
          "Дедлайн прохождения должен иметь обрезанные миллисекунды и наносекунды");

      assertEquals(plainSurveyUnpublished, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.create(SurveyServiceTestIdAndTime.account1Id, plainSurveyUnpublishedCreateDto);
    assertEquals(plainSurveyUnpublishedResponseDto, result);
  }

  @Test
  void create_noExpireAtSet_createCorrectEntity() {
    Mockito.when(accountDao.findById(SurveyServiceTestIdAndTime.account1Id))
        .thenReturn(Optional.of(SurveyServiceTestEntity.account1));
    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertNotNull(survey.getId(), "ID опроса не был заполнен");
      assertTrue(survey.getId().toString().matches(uuidRegex), "ID не является UUID");

      survey.setId(SurveyServiceTestIdAndTime.plainSurveyId);

      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
              && !survey.getCreatedAt().isAfter(Instant.now()),
          "Установленное время создания оказалось в будущем или было слишком давно");
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000,
          "Время создания должно иметь обрезанные миллисекунды и наносекунды");

      survey.setCreatedAt(null);

      assertEquals(SurveyServiceTestEntity.account1, survey.getAuthor(),
          "Автор опроса должен быть установлен правильно");
      assertFalse(survey.isTemplate(),
          "Созданный опрос не должен быть шаблоном");
      assertNull(survey.getExpireAt(),
          "Дедлайн прохождения не должен быть заполнен, если он не был указан");
      assertEquals("Europe/Moscow", survey.getTargetTimezone(),
          "Часовой пояс должен быть установлен в значение по умолчанию");

      assertEquals(plainSurveyUnpublishedNoExpireAt, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.create(SurveyServiceTestIdAndTime.account1Id, plainSurveyUnpublishedNoExpireAtCreateDto);
    assertEquals(plainSurveyUnpublishedNoExpireAtResponseDto, result);
  }
}
