package ru.hh.kakdela.v2.service.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestIdAndTime;

public class SurveyServiceDeleteTest extends SurveyServiceTest {

  @Test
  void delete_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(SurveyServiceTestIdAndTime.fullSurveyId))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.delete(SurveyServiceTestIdAndTime.fullSurveyId, SurveyServiceTestIdAndTime.account1Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + SurveyServiceTestIdAndTime.fullSurveyId + "\"",
        ex.getMessage()
    );
  }

  @Test
  void delete_surveyFound_checkPermissions() {
    Mockito.when(surveyDao.findById(SurveyServiceTestIdAndTime.fullSurveyId))
        .thenReturn(Optional.of(fullSurvey));

    surveyService.delete(SurveyServiceTestIdAndTime.fullSurveyId, SurveyServiceTestIdAndTime.account1Id);

    Mockito.verify(permissionService)
        .checkOwnership(SurveyServiceTestIdAndTime.fullSurveyId, SurveyServiceTestIdAndTime.account1Id);
  }

  @Test
  void delete_deletionPermitted_callDaoDeleteMethod() {
    Mockito.when(surveyDao.findById(SurveyServiceTestIdAndTime.fullSurveyId))
        .thenReturn(Optional.of(fullSurvey));

    surveyService.delete(SurveyServiceTestIdAndTime.fullSurveyId, SurveyServiceTestIdAndTime.account1Id);

    Mockito.verify(surveyDao).delete(fullSurvey);
  }
}
