package ru.hh.kakdela.v2.util.service.survey;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionResponseDto;
import ru.hh.kakdela.v2.dto.closing_page.ClosingPageResponseDto;
import ru.hh.kakdela.v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageResponseDto;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants.FullSurveyConstants;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants.PlainSurveyConstants;

public class SurveyServiceTestDto {

  public static SurveyResponseDto getResponseDtoOfFullSurvey(boolean includeClosingPage, boolean isClone) {
    return new SurveyResponseDto(
        FullSurveyConstants.SURVEY.getId(isClone),
        new AccountResponseDto(
            FullSurveyConstants.getAuthorId(isClone),
            FullSurveyConstants.getAuthorLogin(isClone),
            FullSurveyConstants.getAuthorEmail(isClone)
        ),
        FullSurveyConstants.getTitle(isClone),
        "description",
        false,
        false,
        !isClone,
        false,
        false,
        SurveyServiceTestConstants.expireAtSevenDays,
        SurveyServiceTestConstants.expireAtAtMoscowTimezone,
        "Europe/Moscow",
        null,
        List.of(new SurveyPageResponseDto(
            FullSurveyConstants.PAGE1.getId(isClone),
            FullSurveyConstants.SURVEY.getId(isClone),
            1,
            "surveyPage",
            "description",
            List.of(
                new QuestionResponseDto(
                    FullSurveyConstants.QUESTION1.getId(isClone),
                    1,
                    "question1",
                    "description",
                    SurveyServiceTestConstants.attachmentUrl,
                    Question.QuestionType.SHORT_TEXT.name(),
                    Question.AnswerOptionOrder.ORIGINAL.name(),
                    false,
                    true,
                    true,
                    "condition",
                    Collections.emptyList()),
                new QuestionResponseDto(
                    FullSurveyConstants.QUESTION2.getId(isClone),
                    2,
                    "question2",
                    "description",
                    SurveyServiceTestConstants.attachmentUrl,
                    Question.QuestionType.SINGLE_CHOICE.name(),
                    Question.AnswerOptionOrder.ORIGINAL.name(),
                    false,
                    true,
                    true,
                    "condition",
                    List.of(
                        new AnswerOptionResponseDto(
                            FullSurveyConstants.ANSWER_OPTION1_OF_QUESTION2.getId(isClone),
                            1,
                            "answerOption1",
                            SurveyServiceTestConstants.attachmentUrl),
                        new AnswerOptionResponseDto(
                            FullSurveyConstants.ANSWER_OPTION2_OF_QUESTION2.getId(isClone),
                            2,
                            "answerOption2",
                            SurveyServiceTestConstants.attachmentUrl))),
                new QuestionResponseDto(
                    FullSurveyConstants.QUESTION3.getId(isClone),
                    3,
                    "question3",
                    "description",
                    SurveyServiceTestConstants.attachmentUrl,
                    Question.QuestionType.MULTIPLE_CHOICE.name(),
                    Question.AnswerOptionOrder.ORIGINAL.name(),
                    false,
                    true,
                    true,
                    "condition",
                    List.of(
                        new AnswerOptionResponseDto(
                            FullSurveyConstants.ANSWER_OPTION1_OF_QUESTION3.getId(isClone),
                            1,
                            "answerOption1",
                            SurveyServiceTestConstants.attachmentUrl),
                        new AnswerOptionResponseDto(
                            FullSurveyConstants.ANSWER_OPTION2_OF_QUESTION3.getId(isClone),
                            2,
                            "answerOption2",
                            SurveyServiceTestConstants.attachmentUrl)))))),
        includeClosingPage
            ? new ClosingPageResponseDto(
                "closingPage",
                "description",
                SurveyServiceTestConstants.attachmentUrl,
                "websiteUrl")
            : null);
  }

  public static SurveyResponseDto getResponseDtoOfPlainSurvey(
      boolean otherValuesOfTitleAndDescription,
      boolean allSurveyOptionValues,
      boolean isPublished,
      Instant expireAt,
      LocalDateTime expireAtAtTargetTimezone,
      String targetTimezone
  ) {
    return new SurveyResponseDto(
        SurveyServiceTestConstants.plainSurveyId,
        new AccountResponseDto(
            SurveyServiceTestEntity.account1.getId(),
            SurveyServiceTestEntity.account1.getLogin(),
            SurveyServiceTestEntity.account1.getEmail()
        ),
        PlainSurveyConstants.getTitle(otherValuesOfTitleAndDescription),
        PlainSurveyConstants.getDescription(otherValuesOfTitleAndDescription),
        allSurveyOptionValues,
        allSurveyOptionValues,
        isPublished,
        false,
        allSurveyOptionValues,
        expireAt,
        expireAtAtTargetTimezone,
        targetTimezone,
        null,
        Collections.emptyList(),
        null
    );
  }

  public static SurveyShortResponseWithPermissionDto getShortResponseDtoOfFullSurvey(boolean isClone) {
    return new SurveyShortResponseWithPermissionDto(
        FullSurveyConstants.SURVEY.getId(isClone),
        FullSurveyConstants.getTitle(isClone),
        "description",
        true,
        null,
        Permission.SurveyRole.AUTHOR
    );
  }

  public static SurveyShortResponseWithPermissionDto getShortResponseDtoOfPlainSurvey(
      boolean otherValuesOfTitleAndDescription,
      boolean isPublished
  ) {
    return new SurveyShortResponseWithPermissionDto(
        SurveyServiceTestConstants.plainSurveyId,
        PlainSurveyConstants.getTitle(otherValuesOfTitleAndDescription),
        PlainSurveyConstants.getDescription(otherValuesOfTitleAndDescription),
        isPublished,
        null,
        Permission.SurveyRole.AUTHOR
    );
  }

  public static SurveyCreateDto getCreateDtoForPlainSurvey(
      boolean allSurveyOptionValues,
      LocalDateTime expireAtAtTargetTimezone,
      String targetTimezone
  ) {
    SurveyCreateDto dto = new SurveyCreateDto();

    dto.setTitle("plainSurvey");
    dto.setDescription("description");
    dto.setIsAuthorizedOnly(allSurveyOptionValues);
    dto.setIsLimitedToOneResponse(allSurveyOptionValues);
    dto.setDoNotify(allSurveyOptionValues);
    dto.setExpireAtAtTargetTimezone(expireAtAtTargetTimezone);
    if (targetTimezone != null) {
      dto.setTargetTimezone(targetTimezone);
    }

    return dto;
  }

  public static SurveyUpdateDto getUpdateDtoForPlainSurvey(
      Boolean otherValuesOfTitleAndDescription,
      Boolean allSurveyOptionValues,
      Boolean isPublished,
      LocalDateTime expireAtAtTargetTimezone,
      String targetTimezone
  ) {
    SurveyUpdateDto dto = new SurveyUpdateDto();

    if (otherValuesOfTitleAndDescription != null) {
      dto.setTitle(PlainSurveyConstants.getTitle(otherValuesOfTitleAndDescription));
      dto.setDescription(PlainSurveyConstants.getDescription(otherValuesOfTitleAndDescription));
    }
    if (allSurveyOptionValues != null) {
      dto.setIsAuthorizedOnly(allSurveyOptionValues);
      dto.setIsLimitedToOneResponse(allSurveyOptionValues);
      dto.setDoNotify(allSurveyOptionValues);
    }
    if (isPublished != null) {
      dto.setIsPublished(isPublished);
    }
    if (expireAtAtTargetTimezone != null) {
      dto.setExpireAtAtTargetTimezone(expireAtAtTargetTimezone);
    }
    if (targetTimezone != null) {
      dto.setTargetTimezone(targetTimezone);
    }

    return dto;
  }

  public static SurveyCreateDto getAuthorizationConsistencyValidationCreateDtoForPlainSurvey(
      boolean isAuthorizedOnly,
      boolean isLimitedToOneResponse
  ) {
    SurveyCreateDto dto = new SurveyCreateDto();

    dto.setTitle("plainSurvey");
    dto.setDescription("description");
    dto.setIsAuthorizedOnly(isAuthorizedOnly);
    dto.setIsLimitedToOneResponse(isLimitedToOneResponse);
    dto.setDoNotify(false);
    dto.setExpireAtAtTargetTimezone(null);

    return dto;
  }

  public static SurveyUpdateDto getAuthorizationConsistencyValidationUpdateDtoForPlainSurvey(
      boolean isAuthorizedOnly,
      boolean isLimitedToOneResponse
  ) {
    SurveyUpdateDto dto = new SurveyUpdateDto();

    dto.setIsAuthorizedOnly(isAuthorizedOnly);
    dto.setIsLimitedToOneResponse(isLimitedToOneResponse);

    return dto;
  }
}
