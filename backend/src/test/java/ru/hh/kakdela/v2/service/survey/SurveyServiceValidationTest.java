package ru.hh.kakdela.v2.service.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestEntity;

public class SurveyServiceValidationTest extends SurveyServiceTestBase {


  @Test
  void create_surveyIsInconsistent_throwException() {
    Mockito.when(accountDao.findById(SurveyServiceTestConstants.account1Id))
        .thenReturn(Optional.of(SurveyServiceTestEntity.account1));

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.create(SurveyServiceTestConstants.account1Id,
            plainSurveyAuthorizationInconsistentCreateDto)
    );
    assertEquals(
        "400 BAD_REQUEST \"Опция \"Запретить проходить более одного раза\" доступна только при "
            + "включённой опции \"Запретить анонимное прохождение\"\"",
        ex.getMessage()
    );
  }

  @Test
  void update_Partial_surveyIsInconsistent_throwException() {
    Survey surveyToUpdate = SurveyServiceTestBase.getFreshPlainSurveyUnpublished();

    Mockito.when(surveyDao.findById(SurveyServiceTestConstants.plainSurveyId))
        .thenReturn(Optional.of(surveyToUpdate));

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.updatePartial(SurveyServiceTestConstants.plainSurveyId,
            plainSurveyAuthorizationInconsistentUpdateDto,
            SurveyServiceTestConstants.account1Id)
    );
    assertEquals(
        "400 BAD_REQUEST \"Опция \"Запретить проходить более одного раза\" доступна только при "
            + "включённой опции \"Запретить анонимное прохождение\"\"",
        ex.getMessage()
    );
  }
}
