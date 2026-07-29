package ru.hh.kakdela.v2.service.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants.FullSurveyConstants;

public class SurveyServiceDeleteTest extends SurveyServiceTestBase {

  @Test
  void delete_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.delete(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account1Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + FullSurveyConstants.SURVEY.getId(IS_ORIGINAL) + "\"",
        ex.getMessage()
    );
  }

  @Test
  void delete_surveyFound_checkPermissions() {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.of(fullSurvey));

    surveyService.delete(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account1Id);

    Mockito.verify(permissionService)
        .checkCanDelete(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account1Id);
  }

  @Test
  void delete_deletionPermitted_callDaoDeleteMethod() {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.of(fullSurvey));

    surveyService.delete(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account1Id);

    Mockito.verify(surveyDao).delete(fullSurvey);
  }
}
