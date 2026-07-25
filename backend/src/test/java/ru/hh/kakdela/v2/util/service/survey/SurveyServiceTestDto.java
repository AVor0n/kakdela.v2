package ru.hh.kakdela.v2.util.service.survey;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

public class SurveyServiceTestDto {
  public static SurveyResponseDto getResponseDtoOfFullSurvey(boolean includeClosingPage, boolean isClone) {
    return new SurveyResponseDto(
        !isClone
            ? SurveyServiceTestIdAndTime.fullSurveyId
            : SurveyServiceTestIdAndTime.fullSurveyCloneId,
        !isClone
          ? SurveyServiceTestIdAndTime.account1Id
          : SurveyServiceTestIdAndTime.account2Id,
        !isClone
            ? "fullSurvey"
            : "Копия — fullSurvey",
        "description",
        false,
        false,
        !isClone,
        false,
        false,
        SurveyServiceTestIdAndTime.expireAt,
        SurveyServiceTestIdAndTime.expireAtAtMoscowTimezone,
        "Europe/Moscow",
        null,
        List.of(new SurveyPageResponseDto(
            !isClone
                ? UUID.fromString("e61ab944-4729-4277-af32-893c0470b442")
                : SurveyServiceTestIdAndTime.page1CloneId,
            !isClone
                ? SurveyServiceTestIdAndTime.fullSurveyId
                : SurveyServiceTestIdAndTime.fullSurveyCloneId,
            1,
            "surveyPage",
            "description",
            List.of(
                new QuestionResponseDto(
                    !isClone
                        ? UUID.fromString("deb153eb-6065-4797-9337-a3505a3c33eb")
                        : SurveyServiceTestIdAndTime.question1CloneId,
                    1,
                    "question1",
                    "description",
                    "http://attachmentUrl/",
                    Question.QuestionType.SHORT_TEXT.name(),
                    null,
                    true,
                    true,
                    "condition",
                    Collections.emptyList()),
                new QuestionResponseDto(
                    !isClone
                        ? UUID.fromString("1d0bd6fc-6d26-4830-b731-1ef8ad59e7f0")
                        : SurveyServiceTestIdAndTime.question2CloneId,
                    2,
                    "question2",
                    "description",
                    "http://attachmentUrl/",
                    Question.QuestionType.SINGLE_CHOICE.name(),
                    Question.AnswerOptionOrder.RANDOM.name(),
                    true,
                    true,
                    "condition",
                    List.of(
                        new AnswerOptionResponseDto(
                            !isClone
                                ? UUID.fromString("8789eec6-6ddf-4d40-af21-f6e52dbe14e0")
                                : SurveyServiceTestIdAndTime.answerOption1OfQuestion2CloneId,
                            1,
                            "answerOption1",
                            "http://attachmentUrl/"),
                        new AnswerOptionResponseDto(
                            !isClone
                                ? UUID.fromString("62252b25-5f79-46d3-8b4c-77d2d1152e2b")
                                : SurveyServiceTestIdAndTime.answerOption2OfQuestion2CloneId,
                            2,
                            "answerOption2",
                            "http://attachmentUrl/"))),
                new QuestionResponseDto(
                    !isClone
                        ? UUID.fromString("e1e2fbba-d861-45ec-8d20-6c6edb75a192")
                        : SurveyServiceTestIdAndTime.question3CloneId,
                    3,
                    "question3",
                    "description",
                    "http://attachmentUrl/",
                    Question.QuestionType.MULTIPLE_CHOICE.name(),
                    Question.AnswerOptionOrder.ORIGINAL.name(),
                    true,
                    true,
                    "condition",
                    List.of(
                        new AnswerOptionResponseDto(
                            !isClone
                                ? UUID.fromString("f17be065-b7ed-42ac-a29a-00cca73d9406")
                                : SurveyServiceTestIdAndTime.answerOption1OfQuestion3CloneId,
                            1,
                            "answerOption1",
                            "http://attachmentUrl/"),
                        new AnswerOptionResponseDto(
                            !isClone
                                ? UUID.fromString("c739ac6b-6b9c-4b87-a88f-67a9ed196bba")
                                : SurveyServiceTestIdAndTime.answerOption2OfQuestion3CloneId,
                            2,
                            "answerOption2",
                            "http://attachmentUrl/")))))),
        includeClosingPage
            ? new ClosingPageResponseDto(
                "closingPage",
                "description",
                "http://attachmentUrl/",
                "websiteUrl")
            : null);
  }

  public static SurveyResponseDto getResponseDtoOfPlainSurvey(
      boolean otherValuesOfTitleAndDescription,
      boolean booleanValuesExceptIsPublished,
      boolean isPublished,
      Instant expireAt,
      LocalDateTime expireAtAtTargetTimezone,
      String targetTimezone
  ) {
    return new SurveyResponseDto(
        SurveyServiceTestIdAndTime.plainSurveyId,
        SurveyServiceTestIdAndTime.account1Id,
        !otherValuesOfTitleAndDescription
            ? "plainSurvey"
            : "plainSurvey — updated",
        !otherValuesOfTitleAndDescription
            ? "description"
            : "description — updated",
        booleanValuesExceptIsPublished,
        booleanValuesExceptIsPublished,
        isPublished,
        false,
        booleanValuesExceptIsPublished,
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
        !isClone
            ? SurveyServiceTestIdAndTime.fullSurveyId
            : SurveyServiceTestIdAndTime.fullSurveyCloneId,
        !isClone
            ? "fullSurvey"
            : "Копия — fullSurvey",
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
        SurveyServiceTestIdAndTime.plainSurveyId,
        !otherValuesOfTitleAndDescription
            ? "plainSurvey"
            : "plainSurvey — updated",
        !otherValuesOfTitleAndDescription
            ? "description"
            : "description — updated",
        isPublished,
        null,
        Permission.SurveyRole.AUTHOR
    );
  }

  public static SurveyCreateDto getCreateDtoForPlainSurvey(
      boolean booleanValues,
      LocalDateTime expireAtAtTargetTimezone,
      String targetTimezone
  ) {
    SurveyCreateDto dto = new SurveyCreateDto();

    dto.setTitle("plainSurvey");
    dto.setDescription("description");
    dto.setIsAuthorizedOnly(booleanValues);
    dto.setIsLimitedToOneResponse(booleanValues);
    dto.setDoNotify(booleanValues);
    dto.setExpireAtAtTargetTimezone(expireAtAtTargetTimezone);
    if (targetTimezone != null) {
      dto.setTargetTimezone(targetTimezone);
    }

    return dto;
  }

  public static SurveyUpdateDto getUpdateDtoForPlainSurvey(
      Boolean otherValuesOfTitleAndDescription,
      Boolean booleanValuesExceptIsPublished,
      Boolean isPublished,
      LocalDateTime expireAtAtTargetTimezone,
      String targetTimezone
  ) {
    SurveyUpdateDto dto = new SurveyUpdateDto();

    if (otherValuesOfTitleAndDescription != null) {
      dto.setTitle(!otherValuesOfTitleAndDescription
          ? "plainSurvey"
          : "plainSurvey — updated");
      dto.setDescription(!otherValuesOfTitleAndDescription
          ? "description"
          : "description — updated");
    }
    if (booleanValuesExceptIsPublished != null) {
      dto.setIsAuthorizedOnly(booleanValuesExceptIsPublished);
      dto.setIsLimitedToOneResponse(booleanValuesExceptIsPublished);
      dto.setDoNotify(booleanValuesExceptIsPublished);
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
}
