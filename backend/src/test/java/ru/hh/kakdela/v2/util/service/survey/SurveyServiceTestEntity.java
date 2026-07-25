package ru.hh.kakdela.v2.util.service.survey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Answer;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

public class SurveyServiceTestEntity {
  public static final Account account1 = new Account(
      SurveyServiceTestIdAndTime.account1Id,
        "account1",
            null,
            null,
            null,
            null,
            null,
            null);
  public static final Account account2 = new Account(
      SurveyServiceTestIdAndTime.account2Id,
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
            ? SurveyServiceTestIdAndTime.fullSurveyId
            : SurveyServiceTestIdAndTime.fullSurveyCloneId,
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
        SurveyServiceTestIdAndTime.expireAt,
        "Europe/Moscow",
        null,
        new ArrayList<>(),
        new ArrayList<>(),
        null,
        new ArrayList<>());

    if (!isClone) {
      survey.getPermissions().add(new Permission(
          new Permission.PermissionId(
              SurveyServiceTestIdAndTime.account2Id, SurveyServiceTestIdAndTime.fullSurveyId),
          account2,
          survey,
          Permission.SurveyRole.EDITOR,
          false));
    }

    if (includeClosingPage) {
      survey.setClosingPage(new ClosingPage(
          !isClone
              ? UUID.fromString("8198aea4-3f54-43df-ac27-15d11500a744")
              : SurveyServiceTestIdAndTime.closingPage1CloneId,
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
            : SurveyServiceTestIdAndTime.page1CloneId,
        survey,
        1,
        "surveyPage",
        "description",
        new ArrayList<>());
    Question question1 = new Question(
        !isClone
            ? UUID.fromString("deb153eb-6065-4797-9337-a3505a3c33eb")
            : SurveyServiceTestIdAndTime.question1CloneId,
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
            : SurveyServiceTestIdAndTime.question2CloneId,
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
                : SurveyServiceTestIdAndTime.answerOption1OfQuestion2CloneId,
            question2,
            1,
            "answerOption1",
            "attachmentObjectKey"),
        new AnswerOption(
            !isClone
                ? UUID.fromString("62252b25-5f79-46d3-8b4c-77d2d1152e2b")
                : SurveyServiceTestIdAndTime.answerOption2OfQuestion2CloneId,
            question2,
            2,
            "answerOption2",
            "attachmentObjectKey"));
    question2.setAnswerOptions(question2AnswerOptionList);
    Question question3 = new Question(
        !isClone
            ? UUID.fromString("e1e2fbba-d861-45ec-8d20-6c6edb75a192")
            : SurveyServiceTestIdAndTime.question3CloneId,
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
                : SurveyServiceTestIdAndTime.answerOption1OfQuestion3CloneId,
            question3,
            1,
            "answerOption1",
            "attachmentObjectKey"),
        new AnswerOption(
            !isClone
                ? UUID.fromString("c739ac6b-6b9c-4b87-a88f-67a9ed196bba")
                : SurveyServiceTestIdAndTime.answerOption2OfQuestion3CloneId,
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
        SurveyServiceTestIdAndTime.plainSurveyId,
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
}
