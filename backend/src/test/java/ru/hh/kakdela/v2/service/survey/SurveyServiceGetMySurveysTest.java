package ru.hh.kakdela.v2.service.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyWithUserRoleDto;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestEntity;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestIdAndTime;

public class SurveyServiceGetMySurveysTest extends SurveyServiceTest {

  @Test
  void getMySurveys_surveysNotFound_returnEmptyListOfDto() {
    Mockito.when(permissionService.getAccessibleSurveys(SurveyServiceTestIdAndTime.account1Id))
        .thenReturn(Collections.emptyList());

    List<SurveyShortResponseWithPermissionDto> result = surveyService.getMySurveys(
        SurveyServiceTestIdAndTime.account1Id);
    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void getMySurveys_surveysFound_returnCorrectListOfDto() {
    Mockito.when(permissionService.getAccessibleSurveys(SurveyServiceTestIdAndTime.account1Id))
        .thenReturn(List.of(
            new SurveyWithUserRoleDto(
                SurveyServiceTestEntity.getFullSurvey(true, false), Permission.SurveyRole.AUTHOR),
            new SurveyWithUserRoleDto(plainSurveyUnpublished, Permission.SurveyRole.AUTHOR)
        ));

    List<SurveyShortResponseWithPermissionDto> result = surveyService.getMySurveys(
        SurveyServiceTestIdAndTime.account1Id);
    assertEquals(List.of(fullSurveyShortResponseDto, plainSurveyUnpublishedShortResponseDto), result);
  }
}
