package ru.hh.kakdela.v2.service.survey;

import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dao.SurveyNotificationSubscriptionDao;
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
import ru.hh.kakdela.v2.service.*;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestDto;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestEntity;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTestBase {

  @Mock
  protected SurveyDao surveyDao;
  @Mock
  protected AccountDao accountDao;
  @Mock
  protected SurveyNotificationSubscriptionDao subscriptionDao;
  @Mock
  protected PermissionService permissionService;
  @Mock
  protected NotificationService notificationService;
  @Mock
  protected ObjectStorageService objectStorageService;
  @Mock
  protected ConditionService conditionService;
  @Mock
  protected ImageProcessingService imageProcessingService;

  protected SurveyService surveyService;

  protected static final boolean INCLUDE_CLOSING_PAGE = true;
  protected static final boolean WITHOUT_CLOSING_PAGE = false;

  protected static final boolean IS_CLONE = true;
  protected static final boolean IS_ORIGINAL = false;

  protected static final boolean OTHER_TITLE_AND_DESCRIPTION = true;
  protected static final boolean DEFAULT_TITLE_AND_DESCRIPTION = false;
  protected static final Boolean TITLE_AND_DESCRIPTION_NO_CHANGES = null;

  protected static final boolean PUBLISHED = true;
  protected static final boolean UNPUBLISHED = false;
  protected static final Boolean PUBLICATION_STATUS_NO_CHANGES = null;

  protected static final boolean ALL_SURVEY_OPTIONS_ARE_TRUE = true;
  protected static final boolean ALL_SURVEY_OPTIONS_ARE_FALSE = false;
  protected static final Boolean ALL_SURVEY_OPTIONS_NO_CHANGES = null;

  protected static final LocalDateTime NO_EXPIRE_AT = null;
  protected static final Instant NO_EXPIRE_AT_INSTANT = null;
  protected static final LocalDateTime EXPIRE_AT_NO_CHANGES = null;

  protected static final String NO_TARGET_TIMEZONE = null;
  protected static final String TARGET_TIMEZONE_NO_CHANGES = null;

  protected static final boolean FOR_ANY_USERS = false;
  protected static final boolean LIMITED_TO_ONE_RESPONSE = true;

  protected static final Survey fullSurvey =
      SurveyServiceTestEntity.getFullSurvey(INCLUDE_CLOSING_PAGE, IS_ORIGINAL);
  protected static final SurveyResponseDto fullSurveyResponseDto =
      SurveyServiceTestDto.getResponseDtoOfFullSurvey(INCLUDE_CLOSING_PAGE, IS_ORIGINAL);
  protected static final SurveyShortResponseWithPermissionDto fullSurveyShortResponseDto =
      SurveyServiceTestDto.getShortResponseDtoOfFullSurvey(IS_ORIGINAL);

  protected static final Survey fullSurveyWithoutClosingPage =
      SurveyServiceTestEntity.getFullSurvey(WITHOUT_CLOSING_PAGE, IS_ORIGINAL);

  protected static final Survey fullSurveyClone =
      SurveyServiceTestEntity.getFullSurvey(INCLUDE_CLOSING_PAGE, IS_CLONE);
  protected static final SurveyResponseDto fullSurveyCloneResponseDto =
      SurveyServiceTestDto.getResponseDtoOfFullSurvey(INCLUDE_CLOSING_PAGE, IS_CLONE);

  protected static final Survey fullSurveyCloneWithoutClosingPage =
      SurveyServiceTestEntity.getFullSurvey(WITHOUT_CLOSING_PAGE, IS_CLONE);
  protected static final SurveyResponseDto fullSurveyCloneWithoutClosingPageResponseDto =
      SurveyServiceTestDto.getResponseDtoOfFullSurvey(WITHOUT_CLOSING_PAGE, IS_CLONE);

  protected static final Survey plainSurveyUnpublished =
      SurveyServiceTestEntity.getPlainSurvey(
          DEFAULT_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          UNPUBLISHED,
          SurveyServiceTestConstants.expireAtSevenDays,
          "Asia/Yekaterinburg");
  protected static final SurveyResponseDto plainSurveyUnpublishedResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          DEFAULT_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          UNPUBLISHED,
          SurveyServiceTestConstants.expireAtSevenDays,
          SurveyServiceTestConstants.expireAtAtYekaterinburgTimezone,
          "Asia/Yekaterinburg");
  protected static final SurveyShortResponseWithPermissionDto
      plainSurveyUnpublishedShortResponseDto =
      SurveyServiceTestDto.getShortResponseDtoOfPlainSurvey(DEFAULT_TITLE_AND_DESCRIPTION, UNPUBLISHED);
  protected static final SurveyCreateDto plainSurveyUnpublishedCreateDto =
      SurveyServiceTestDto.getCreateDtoForPlainSurvey(
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          SurveyServiceTestConstants.expireAtAtYekaterinburgTimezone,
          "Asia/Yekaterinburg");
  protected static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoNoChanges =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          TITLE_AND_DESCRIPTION_NO_CHANGES,
          ALL_SURVEY_OPTIONS_NO_CHANGES,
          PUBLICATION_STATUS_NO_CHANGES,
          EXPIRE_AT_NO_CHANGES,
          TARGET_TIMEZONE_NO_CHANGES);
  protected static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoPublished =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          TITLE_AND_DESCRIPTION_NO_CHANGES,
          ALL_SURVEY_OPTIONS_NO_CHANGES,
          PUBLISHED,
          EXPIRE_AT_NO_CHANGES,
          TARGET_TIMEZONE_NO_CHANGES);

  protected static final Survey plainSurveyUnpublishedNoExpireAt =
      SurveyServiceTestEntity.getPlainSurvey(
          DEFAULT_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          UNPUBLISHED,
          NO_EXPIRE_AT_INSTANT,
          "Europe/Moscow");
  protected static final SurveyResponseDto plainSurveyUnpublishedNoExpireAtResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          DEFAULT_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          UNPUBLISHED,
          NO_EXPIRE_AT_INSTANT,
          NO_EXPIRE_AT,
          "Europe/Moscow");
  protected static final SurveyCreateDto plainSurveyUnpublishedNoExpireAtCreateDto =
      SurveyServiceTestDto.getCreateDtoForPlainSurvey(
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          NO_EXPIRE_AT,
          NO_TARGET_TIMEZONE);

  protected static final Survey plainSurveyUnpublishedAnotherExpireAt =
      SurveyServiceTestEntity.getPlainSurvey(
          DEFAULT_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          UNPUBLISHED,
          SurveyServiceTestConstants.anotherExpireAtPlusHour,
          "Asia/Yekaterinburg");
  protected static final SurveyResponseDto plainSurveyUnpublishedAnotherExpireAtResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          DEFAULT_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          UNPUBLISHED,
          SurveyServiceTestConstants.anotherExpireAtPlusHour,
          SurveyServiceTestConstants.anotherExpireAtAtYekaterinburgTimezone,
          "Asia/Yekaterinburg");
  protected static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoAnotherExpireAt =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          TITLE_AND_DESCRIPTION_NO_CHANGES,
          ALL_SURVEY_OPTIONS_NO_CHANGES,
          PUBLICATION_STATUS_NO_CHANGES,
          SurveyServiceTestConstants.anotherExpireAtAtYekaterinburgTimezone,
          TARGET_TIMEZONE_NO_CHANGES);

  protected static final Survey plainSurveyUnpublishedAnotherTargetTimezone =
      SurveyServiceTestEntity.getPlainSurvey(
          DEFAULT_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          UNPUBLISHED,
          SurveyServiceTestConstants.expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka,
          "Asia/Kamchatka");
  protected static final SurveyResponseDto plainSurveyUnpublishedAnotherTargetTimezoneResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          DEFAULT_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_FALSE,
          UNPUBLISHED,
          SurveyServiceTestConstants.expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka,
          SurveyServiceTestConstants.expireAtAtYekaterinburgTimezone, "Asia/Kamchatka");
  protected static final SurveyUpdateDto plainSurveyUnpublishedUpdateDtoAnotherTargetTimezone =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          TITLE_AND_DESCRIPTION_NO_CHANGES,
          ALL_SURVEY_OPTIONS_NO_CHANGES,
          PUBLICATION_STATUS_NO_CHANGES,
          EXPIRE_AT_NO_CHANGES,
          "Asia/Kamchatka");

  protected static final Survey plainSurveyUnpublishedOtherValuesExceptIsPublished =
      SurveyServiceTestEntity.getPlainSurvey(
          OTHER_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_TRUE,
          UNPUBLISHED,
          SurveyServiceTestConstants.anotherExpireAtPlusHour,
          "Asia/Kamchatka");
  protected static final SurveyResponseDto plainSurveyOtherValuesExceptIsPublishedResponseDto =
      SurveyServiceTestDto.getResponseDtoOfPlainSurvey(
          OTHER_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_TRUE,
          UNPUBLISHED,
          SurveyServiceTestConstants.anotherExpireAtPlusHour,
          SurveyServiceTestConstants.anotherExpireAtAtKamchatkaTimezone,
          "Asia/Kamchatka");
  protected static final SurveyUpdateDto
      plainSurveyUnpublishedUpdateDtoOtherValuesExceptIsPublished =
      SurveyServiceTestDto.getUpdateDtoForPlainSurvey(
          OTHER_TITLE_AND_DESCRIPTION,
          ALL_SURVEY_OPTIONS_ARE_TRUE,
          PUBLICATION_STATUS_NO_CHANGES,
          SurveyServiceTestConstants.anotherExpireAtAtKamchatkaTimezone,
          "Asia/Kamchatka");

  protected static final SurveyCreateDto plainSurveyAuthorizationInconsistentCreateDto =
      SurveyServiceTestDto.getAuthorizationConsistencyValidationCreateDtoForPlainSurvey(
          FOR_ANY_USERS,
          LIMITED_TO_ONE_RESPONSE
      );
  protected static final SurveyUpdateDto plainSurveyAuthorizationInconsistentUpdateDto =
      SurveyServiceTestDto.getAuthorizationConsistencyValidationUpdateDtoForPlainSurvey(
          FOR_ANY_USERS,
          LIMITED_TO_ONE_RESPONSE
      );

  protected static final String uuidRegex =
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

  protected static Survey getFreshPlainSurveyUnpublished() {
    return SurveyServiceTestEntity.getPlainSurvey(
        DEFAULT_TITLE_AND_DESCRIPTION,
        ALL_SURVEY_OPTIONS_ARE_FALSE,
        UNPUBLISHED,
        SurveyServiceTestConstants.expireAtSevenDays,
        "Asia/Yekaterinburg");
  }

  protected static Survey getFreshPlainSurveyPublished() {
    return SurveyServiceTestEntity.getPlainSurvey(
        DEFAULT_TITLE_AND_DESCRIPTION,
        ALL_SURVEY_OPTIONS_ARE_FALSE,
        PUBLISHED,
        SurveyServiceTestConstants.expireAtSevenDays,
        "Asia/Yekaterinburg");
  }

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
        subscriptionDao,
        permissionService,
        notificationService,
        objectStorageService,
        conditionService,
        imageProcessingService,
        surveyMapper
    );
  }
}
