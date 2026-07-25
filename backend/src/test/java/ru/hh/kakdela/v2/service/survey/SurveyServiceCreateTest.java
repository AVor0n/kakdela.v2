package ru.hh.kakdela.v2.service.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

      // Проверка автоматической генерации ID
      assertNotEquals(null, survey.getId());
      assertTrue(survey.getId().toString().matches(uuidRegex));

      survey.setId(SurveyServiceTestIdAndTime.plainSurveyId);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      // Проверка автора
      assertEquals(SurveyServiceTestEntity.account1, survey.getAuthor());
      // Созданный опрос не должен быть шаблоном
      assertFalse(survey.isTemplate());
      // Проверка дедлайна прохождения
      assertEquals(plainSurveyUnpublished.getExpireAt(), survey.getExpireAt());
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000);

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

      // Проверка автоматической генерации ID
      assertNotEquals(null, survey.getId());
      assertTrue(survey.getId().toString().matches(uuidRegex));

      survey.setId(SurveyServiceTestIdAndTime.plainSurveyId);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      // Проверка автора
      assertEquals(SurveyServiceTestEntity.account1, survey.getAuthor());
      // Созданный опрос не должен быть шаблоном
      assertFalse(survey.isTemplate());
      // Проверка дедлайна прохождения
      assertNull(survey.getExpireAt());
      // Часовой пояс должен быть установлен в значение по умолчанию
      assertEquals("Europe/Moscow", survey.getTargetTimezone());

      assertEquals(plainSurveyUnpublishedNoExpireAt, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.create(SurveyServiceTestIdAndTime.account1Id, plainSurveyUnpublishedNoExpireAtCreateDto);
    assertEquals(plainSurveyUnpublishedNoExpireAtResponseDto, result);
  }
}
