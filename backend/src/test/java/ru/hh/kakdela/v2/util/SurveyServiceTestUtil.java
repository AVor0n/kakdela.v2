package ru.hh.kakdela.v2.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Answer;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

public class SurveyServiceTestUtil {

  public static final UUID account1Id = UUID.fromString("5f456068-7941-4188-b7ec-8d2b2ad38b68");
  public static final UUID account2Id = UUID.fromString("a14a1088-3603-466f-a07e-c187daed72d4");
  public static final UUID fullSurveyId = UUID.fromString("41f26c0d-340f-4958-8ac3-8c5ef2299bdf");
  public static final UUID plainSurveyId = UUID.fromString("9f5fb2af-c1ef-4666-830a-f0452c6c7b67");
  public static final UUID fullSurveyCloneId = UUID.fromString("3569dadd-8ffb-46a9-8d7e-3dd6882da658");
  public static final UUID page1CloneId = UUID.fromString("26f03c3e-3e6d-4c11-88e4-63fc4af7e990");
  public static final UUID question1CloneId = UUID.fromString("b10a74e1-01c3-4c60-ae83-970d20318fee");
  public static final UUID question2CloneId = UUID.fromString("a0fda026-f590-4f6a-8b88-e44805b9a349");
  public static final UUID question3CloneId = UUID.fromString("1d6015f5-02fd-475b-a390-b55a6334b33b");
  public static final UUID answerOption1OfQuestion2CloneId = UUID.fromString("f08209f3-71c2-454d-aaaf-d6d15e7448d5");
  public static final UUID answerOption2OfQuestion2CloneId = UUID.fromString("0fba2523-b47f-424f-a62a-fdb2529dd8cf");
  public static final UUID answerOption1OfQuestion3CloneId = UUID.fromString("b4fa920c-05ca-415a-b17f-dadb4358a1fd");
  public static final UUID answerOption2OfQuestion3CloneId = UUID.fromString("12932aff-06b8-4661-99ae-867129500649");
  public static final UUID closingPage1CloneId = UUID.fromString("0e56a5e6-a219-4dfe-9bc4-80a13cfe06b0");

  public static final Instant expireAt = Instant.now()
      .plus(7, ChronoUnit.DAYS)
      .truncatedTo(ChronoUnit.SECONDS);
  public static final Instant anotherExpireAt = expireAt.plus(Duration.ofHours(1));
  // Разница между Asia/Yekateringurg и Asia/Kamchatka составляет +7 часов в любое время года.
  // Значение переменной ниже на 7 часов больше, чем у expireAt.
  // При этом её значение в Asia/Kamchatka (UTC+12) равно значению expireAt в Asia/Yekateringurg (UTC+5)
  public static final Instant expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka = expireAt
      .atZone(ZoneId.of("Asia/Yekaterinburg"))
      .toLocalDateTime()
      .atZone(ZoneId.of("Asia/Kamchatka"))
      .toInstant();
  public static final LocalDateTime expireAtAtMoscowTimezone = expireAt
      .atZone(ZoneId.of("Europe/Moscow"))
      .toLocalDateTime();
  public static final LocalDateTime expireAtAtYekaterinburgTimezone = expireAt
      .atZone(ZoneId.of("Asia/Yekaterinburg"))
      .toLocalDateTime();
  public static final LocalDateTime anotherExpireAtAtYekaterinburgTimezone = anotherExpireAt
      .atZone(ZoneId.of("Asia/Yekaterinburg"))
      .toLocalDateTime();
  public static final LocalDateTime anotherExpireAtAtKamchatkaTimezone = anotherExpireAt
      .atZone(ZoneId.of("Asia/Kamchatka"))
      .toLocalDateTime();
  
  public static final Account account1 = new Account(
      account1Id,
        "account1",
            null,
            null,
            null,
            null,
            null,
            null);
  public static final Account account2 = new Account(
      account2Id,
        "account2",
            null,
            null,
            null,
            null,
            null,
            null);

  public static Survey getFullSurvey(boolean includeClosingPage, boolean isClone) {
    Survey survey = new Survey(
        !isClone
            ? fullSurveyId
            : fullSurveyCloneId,
        !isClone
            ? account1
            : account2,
        !isClone
            ? "fullSurvey"
            : "Копия — fullSurvey",
        "description",
        false,
        false,
        !isClone,
        false,
        false,
        expireAt,
        "Europe/Moscow",
        null,
        new ArrayList<>(),
        new ArrayList<>(),
        null,
        new ArrayList<>());

    if (!isClone) {
      survey.getPermissions().add(new Permission(
          new Permission.PermissionId(account2Id, fullSurveyId),
          account2,
          survey,
          Permission.SurveyRole.EDITOR,
          false));
    }

    if (includeClosingPage) {
      survey.setClosingPage(new ClosingPage(
          !isClone
              ? UUID.fromString("8198aea4-3f54-43df-ac27-15d11500a744")
              : closingPage1CloneId,
          survey,
          "closingPage",
          "description",
          "attachmentObjectKey",
          "websiteUrl"));
    }

    // Заполнение страниц и вопросов для Survey1

    SurveyPage surveyPage1 = new SurveyPage(
        !isClone
            ? UUID.fromString("e61ab944-4729-4277-af32-893c0470b442")
            : page1CloneId,
        survey,
        1,
        "surveyPage",
        "description",
        new ArrayList<>());
    Question question1 = new Question(
        !isClone
            ? UUID.fromString("deb153eb-6065-4797-9337-a3505a3c33eb")
            : question1CloneId,
        surveyPage1,
        1,
        "question1",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SHORT_TEXT,
        null,
        true,
        true,
        "condition",
        Collections.emptyList(),
        new ArrayList<>());
    Question question2 = new Question(
        !isClone
            ? UUID.fromString("1d0bd6fc-6d26-4830-b731-1ef8ad59e7f0")
            : question2CloneId,
        surveyPage1,
        2,
        "question2",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SINGLE_CHOICE,
        Question.AnswerOptionOrder.RANDOM,
        true,
        true,
        "condition",
        null,
        new ArrayList<>());
    List<AnswerOption> question2AnswerOptionList = List.of(
        new AnswerOption(
            !isClone
                ? UUID.fromString("8789eec6-6ddf-4d40-af21-f6e52dbe14e0")
                : answerOption1OfQuestion2CloneId,
            question2,
            1,
            "answerOption1",
            "attachmentObjectKey"),
        new AnswerOption(
            !isClone
                ? UUID.fromString("62252b25-5f79-46d3-8b4c-77d2d1152e2b")
                : answerOption2OfQuestion2CloneId,
            question2,
            2,
            "answerOption2",
            "attachmentObjectKey"));
    question2.setAnswerOptions(question2AnswerOptionList);
    Question question3 = new Question(
        !isClone
            ? UUID.fromString("e1e2fbba-d861-45ec-8d20-6c6edb75a192")
            : question3CloneId,
        surveyPage1,
        3,
        "question3",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.MULTIPLE_CHOICE,
        Question.AnswerOptionOrder.ORIGINAL,
        true,
        true,
        "condition",
        null,
        new ArrayList<>());
    List<AnswerOption> question3AnswerOptionList = List.of(
        new AnswerOption(
            !isClone
                ? UUID.fromString("f17be065-b7ed-42ac-a29a-00cca73d9406")
                : answerOption1OfQuestion3CloneId,
            question3,
            1,
            "answerOption1",
            "attachmentObjectKey"),
        new AnswerOption(
            !isClone
                ? UUID.fromString("c739ac6b-6b9c-4b87-a88f-67a9ed196bba")
                : answerOption2OfQuestion3CloneId,
            question3,
            2,
            "answerOption2",
            "attachmentObjectKey"));
    question3.setAnswerOptions(question3AnswerOptionList);
    surveyPage1.getQuestions().add(question1);
    surveyPage1.getQuestions().add(question2);
    surveyPage1.getQuestions().add(question3);
    survey.getPages().add(surveyPage1);

    // Заполнение ответов

    if (!isClone) {
      Response response1 = new Response(
          UUID.fromString("1ac091de-05f5-449a-8fd2-e7f54c0a4fe6"),
          account2,
          survey,
          false,
          null,
          new ArrayList<>());
      Answer answer1 = new Answer(
          new Answer.AnswerId(response1.getId(), question1.getId()),
          response1,
          question1,
          "answer1");
      question1.getAnswers().add(answer1);
      Answer answer2 = new Answer(
          new Answer.AnswerId(response1.getId(), question2.getId()),
          response1,
          question2,
          "answerOption1");
      question2.getAnswers().add(answer2);
      Answer answer3 = new Answer(
          new Answer.AnswerId(response1.getId(), question3.getId()),
          response1,
          question3,
          "answerOption2");
      question3.getAnswers().add(answer3);
      response1.getAnswers().add(answer1);
      response1.getAnswers().add(answer2);
      response1.getAnswers().add(answer3);
      survey.getResponses().add(response1);
    }

    return survey;
  }

  public static Survey getPlainSurvey(
      boolean otherValuesOfTitleAndDescription,
      boolean booleanValuesExceptIsPublished,
      boolean isPublished,
      Instant expireAt,
      String targetTimezone
  ) {
    return new Survey(
        plainSurveyId,
        account1,
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
        targetTimezone,
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );
  }

  public static SurveyResponseDto getResponseDtoOfFullSurvey(boolean includeClosingPage, boolean isClone) {
    return new SurveyResponseDto(
        !isClone
            ? fullSurveyId
            : fullSurveyCloneId,
        !isClone
          ? account1Id
          : account2Id,
        !isClone
            ? "fullSurvey"
            : "Копия — fullSurvey",
        "description",
        false,
        false,
        !isClone,
        false,
        false,
        expireAt,
        expireAtAtMoscowTimezone,
        "Europe/Moscow",
        null,
        List.of(new SurveyPageResponseDto(
            !isClone
                ? UUID.fromString("e61ab944-4729-4277-af32-893c0470b442")
                : page1CloneId,
            !isClone
                ? fullSurveyId
                : fullSurveyCloneId,
            1,
            "surveyPage",
            "description",
            List.of(
                new QuestionResponseDto(
                    !isClone
                        ? UUID.fromString("deb153eb-6065-4797-9337-a3505a3c33eb")
                        : question1CloneId,
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
                        : question2CloneId,
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
                                : answerOption1OfQuestion2CloneId,
                            1,
                            "answerOption1",
                            "http://attachmentUrl/"),
                        new AnswerOptionResponseDto(
                            !isClone
                                ? UUID.fromString("62252b25-5f79-46d3-8b4c-77d2d1152e2b")
                                : answerOption2OfQuestion2CloneId,
                            2,
                            "answerOption2",
                            "http://attachmentUrl/"))),
                new QuestionResponseDto(
                    !isClone
                        ? UUID.fromString("e1e2fbba-d861-45ec-8d20-6c6edb75a192")
                        : question3CloneId,
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
                                : answerOption1OfQuestion3CloneId,
                            1,
                            "answerOption1",
                            "http://attachmentUrl/"),
                        new AnswerOptionResponseDto(
                            !isClone
                                ? UUID.fromString("c739ac6b-6b9c-4b87-a88f-67a9ed196bba")
                                : answerOption2OfQuestion3CloneId,
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
        plainSurveyId,
        account1Id,
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
            ? fullSurveyId
            : fullSurveyCloneId,
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
        plainSurveyId,
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
