package ru.hh.kakdela.v2.service.survey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela.v2.mapper.AnswerOptionMapper;
import ru.hh.kakdela.v2.mapper.ClosingPageMapper;
import ru.hh.kakdela.v2.mapper.QuestionMapper;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.mapper.SurveyPageMapper;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.service.NotificationService;
import ru.hh.kakdela.v2.service.ObjectStorageService;
import ru.hh.kakdela.v2.service.PermissionService;
import ru.hh.kakdela.v2.service.SurveyService;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestDto;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestEntity;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestIdAndTime;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

  @Mock
  protected SurveyDao surveyDao;
  @Mock
  protected AccountDao accountDao;
  @Mock
  protected PermissionService permissionService;
  @Mock
  protected NotificationService notificationService;
  @Mock
  protected ObjectStorageService objectStorageService;

  protected SurveyService surveyService;

  protected static final Survey fullSurvey =
      SurveyServiceTestEntity.getFullSurvey(true, false);
  protected static final SurveyResponseDto fullSurveyResponseDto =
      SurveyServiceTestDto.getResponseDtoOfFullSurvey(true, false);
  protected static final SurveyShortResponseWithPermissionDto fullSurveyShortResponseDto =
      SurveyServiceTestDto.getShortResponseDtoOfFullSurvey(false);

  protected static final Survey fullSurveyWithoutClosingPage =
      SurveyServiceTestEntity.getFullSurvey(false, false);

  protected static final Survey fullSurveyClone =
      SurveyServiceTestEntity.getFullSurvey(true, true);
  protected static final SurveyResponseDto fullSurveyCloneResponseDto =
      SurveyServiceTestDto.getResponseDtoOfFullSurvey(true, true);

  protected static final Survey fullSurveyCloneWithoutClosingPage =
      SurveyServiceTestEntity.getFullSurvey(false, true);
  protected static final SurveyResponseDto fullSurveyCloneWithoutClosingPageResponseDto =
      SurveyServiceTestDto.getResponseDtoOfFullSurvey(false, true);

  protected static final Survey plainSurveyUnpublished =
      SurveyServiceTestEntity.getPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestIdAndTime.expireAt,
          "Asia/Yekaterinburg");
  protected static final SurveyResponseDto plainSurveyUnpublishedResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestIdAndTime.expireAt,
          SurveyServiceTestIdAndTime.expireAtAtYekaterinburgTimezone,
          "Asia/Yekaterinburg");
  protected static final SurveyShortResponseWithPermissionDto
      plainSurveyUnpublishedShortResponseDto =
      SurveyServiceTestDto.getShortResponseDtoOfPlainSurvey(false, false);
  protected static final SurveyCreateDto plainSurveyUnpublishedCreateDto =
      SurveyServiceTestDto.getCreateDtoForPlainSurvey(
          false,
          SurveyServiceTestIdAndTime.expireAtAtYekaterinburgTimezone,
          "Asia/Yekaterinburg");
  protected static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoNoChanges =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          null,
          null,
          null,
          null,
          null);
  protected static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoPublished =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          null,
          null,
          true,
          null,
          null);

  protected static final Survey plainSurveyUnpublishedNoExpireAt =
      SurveyServiceTestEntity.getPlainSurvey(
          false,
          false,
          false,
          null,
          "Europe/Moscow");
  protected static final SurveyResponseDto plainSurveyUnpublishedNoExpireAtResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          false,
          false,
          false,
          null,
          null,
          "Europe/Moscow");
  protected static final SurveyCreateDto plainSurveyUnpublishedNoExpireAtCreateDto =
      SurveyServiceTestDto.getCreateDtoForPlainSurvey(
          false,
          null,
          null);

  protected static final Survey plainSurveyUnpublishedAnotherExpireAt =
      SurveyServiceTestEntity.getPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestIdAndTime.anotherExpireAt,
          "Asia/Yekaterinburg");
  protected static final SurveyResponseDto plainSurveyUnpublishedAnotherExpireAtResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestIdAndTime.anotherExpireAt,
          SurveyServiceTestIdAndTime.anotherExpireAtAtYekaterinburgTimezone,
          "Asia/Yekaterinburg");
  protected static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoAnotherExpireAt =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          null,
          null,
          null,
          SurveyServiceTestIdAndTime.anotherExpireAtAtYekaterinburgTimezone,
          null);

  protected static final Survey plainSurveyUnpublishedAnotherTargetTimezone =
      SurveyServiceTestEntity.getPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestIdAndTime.expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka,
          "Asia/Kamchatka");
  protected static final SurveyResponseDto plainSurveyUnpublishedAnotherTargetTimezoneResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          false,
          false,
          false,
          SurveyServiceTestIdAndTime.expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka,
          SurveyServiceTestIdAndTime.expireAtAtYekaterinburgTimezone, "Asia/Kamchatka");
  protected static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoAnotherTargetTimezone =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          null,
          null,
          null,
          null,
          "Asia/Kamchatka");

  protected static final Survey plainSurveyUnpublishedOtherValuesExceptIsPublished =
      SurveyServiceTestEntity.getPlainSurvey(
          true,
          true,
          false,
          SurveyServiceTestIdAndTime.anotherExpireAt,
          "Asia/Kamchatka");
  protected static final SurveyResponseDto plainSurveyOtherValuesExceptIsPublishedResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          true,
          true,
          false,
          SurveyServiceTestIdAndTime.anotherExpireAt,
          SurveyServiceTestIdAndTime.anotherExpireAtAtKamchatkaTimezone,
          "Asia/Kamchatka");
  protected static final SurveyUpdateDto
      plainSurveyUnpublishedUpdateDtoOtherValuesExceptIsPublished =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          true,
          true,
          null,
          SurveyServiceTestIdAndTime.anotherExpireAtAtKamchatkaTimezone,
          "Asia/Kamchatka");

  protected static final String uuidRegex =
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

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
}
