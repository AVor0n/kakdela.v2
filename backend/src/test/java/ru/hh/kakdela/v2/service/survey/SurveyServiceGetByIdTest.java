package ru.hh.kakdela.v2.service.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants.FullSurveyConstants;

@ExtendWith(MockitoExtension.class)
public class SurveyServiceGetByIdTest extends SurveyServiceTestBase {

  @Test
  void getById_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.getById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL),
            SurveyServiceTestConstants.account1Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: id=" + FullSurveyConstants.SURVEY.getId(IS_ORIGINAL) + "\"",
        ex.getMessage()
    );
  }

  @Test
  void getById_surveyNotPublished_checkPermissions() throws MalformedURLException {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_CLONE)))
        .thenReturn(Optional.of(fullSurveyClone));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    surveyService.getById(FullSurveyConstants.SURVEY.getId(IS_CLONE),
        SurveyServiceTestConstants.account1Id);

    Mockito.verify(permissionService).checkHasAnyPermission(
        FullSurveyConstants.SURVEY.getId(IS_CLONE),
        SurveyServiceTestConstants.account1Id);
  }

  @Test
  void getById_surveyPublished_returnCorrectDto() throws MalformedURLException {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.of(fullSurvey));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());
    Mockito.when(objectStorageService.getFileSize(Mockito.anyString()))
        .thenReturn(102400L);

    SurveyResponseDto result = surveyService.getById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL),
        SurveyServiceTestConstants.account1Id);
    assertEquals(fullSurveyResponseDto, result);
  }
}
