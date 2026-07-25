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
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestIdAndTime;

@ExtendWith(MockitoExtension.class)
public class SurveyServiceGetByIdTest extends SurveyServiceTest {

  @Test
  void getById_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(SurveyServiceTestIdAndTime.fullSurveyId))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.getById(SurveyServiceTestIdAndTime.fullSurveyId)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + SurveyServiceTestIdAndTime.fullSurveyId + "\"",
        ex.getMessage()
    );
  }

  @Test
  void getById_surveyFound_returnCorrectDto() throws MalformedURLException {
    Mockito.when(surveyDao.findById(SurveyServiceTestIdAndTime.fullSurveyId))
        .thenReturn(Optional.of(fullSurvey));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    SurveyResponseDto result = surveyService.getById(SurveyServiceTestIdAndTime.fullSurveyId);
    assertEquals(fullSurveyResponseDto, result);
  }
}
