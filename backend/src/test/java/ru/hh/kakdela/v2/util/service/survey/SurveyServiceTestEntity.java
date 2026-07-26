package ru.hh.kakdela.v2.util.service.survey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Answer;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants.FullSurveyConstants;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants.PlainSurveyConstants;

public class SurveyServiceTestEntity {

  private static final boolean IS_ORIGINAL = false;

  public static final Account account1 = new Account(
      SurveyServiceTestConstants.account1Id,
        "account1",
            null,
            null,
            null,
            null,
            null,
            null);
  public static final Account account2 = new Account(
      SurveyServiceTestConstants.account2Id,
        "account2",
            null,
            null,
            null,
            null,
            null,
            null);

  private static Account getAuthorFor(boolean isClone) {
    return isClone
        ? account2
        : account1;
  }

  public static Survey getFullSurvey(boolean includeClosingPage, boolean isClone) {
    Survey survey = new Survey(
        FullSurveyConstants.SURVEY.getId(isClone),
        getAuthorFor(isClone),
        FullSurveyConstants.getTitle(isClone),
        "description",
        false,
        false,
        !isClone,
        false,
        false,
        SurveyServiceTestConstants.expireAtSevenDays,
        "Europe/Moscow",
        null,
        new ArrayList<>(),
        new ArrayList<>(),
        null,
        new ArrayList<>());

    if (!isClone) {
      survey.getPermissions().add(new Permission(
          new Permission.PermissionId(
              SurveyServiceTestConstants.account2Id, FullSurveyConstants.SURVEY.getId(
              IS_ORIGINAL)),
          account2,
          survey,
          Permission.SurveyRole.EDITOR,
          false));
    }

    if (includeClosingPage) {
      survey.setClosingPage(new ClosingPage(
          FullSurveyConstants.CLOSING_PAGE.getId(isClone),
          survey,
          "closingPage",
          "description",
          SurveyServiceTestConstants.attachmentObjectKey,
          "websiteUrl"));
    }

    // Заполнение страниц и вопросов для Survey1

    SurveyPage surveyPage1 = new SurveyPage(
        FullSurveyConstants.PAGE1.getId(isClone),
        survey,
        1,
        "surveyPage",
        "description",
        new ArrayList<>());
    Question question1 = new Question(
        FullSurveyConstants.QUESTION1.getId(isClone),
        surveyPage1,
        1,
        "question1",
        "description",
        SurveyServiceTestConstants.attachmentObjectKey,
        Question.QuestionType.SHORT_TEXT,
        null,
        true,
        true,
        "condition",
        Collections.emptyList(),
        new ArrayList<>());
    Question question2 = new Question(
        FullSurveyConstants.QUESTION2.getId(isClone),
        surveyPage1,
        2,
        "question2",
        "description",
        SurveyServiceTestConstants.attachmentObjectKey,
        Question.QuestionType.SINGLE_CHOICE,
        Question.AnswerOptionOrder.RANDOM,
        true,
        true,
        "condition",
        null,
        new ArrayList<>());
    List<AnswerOption> question2AnswerOptionList = List.of(
        new AnswerOption(
            FullSurveyConstants.ANSWER_OPTION1_OF_QUESTION2.getId(isClone),
            question2,
            1,
            "answerOption1",
            SurveyServiceTestConstants.attachmentObjectKey),
        new AnswerOption(
            FullSurveyConstants.ANSWER_OPTION2_OF_QUESTION2.getId(isClone),
            question2,
            2,
            "answerOption2",
            SurveyServiceTestConstants.attachmentObjectKey));
    question2.setAnswerOptions(question2AnswerOptionList);
    Question question3 = new Question(
        FullSurveyConstants.QUESTION3.getId(isClone),
        surveyPage1,
        3,
        "question3",
        "description",
        SurveyServiceTestConstants.attachmentObjectKey,
        Question.QuestionType.MULTIPLE_CHOICE,
        Question.AnswerOptionOrder.ORIGINAL,
        true,
        true,
        "condition",
        null,
        new ArrayList<>());
    List<AnswerOption> question3AnswerOptionList = List.of(
        new AnswerOption(
            FullSurveyConstants.ANSWER_OPTION1_OF_QUESTION3.getId(isClone),
            question3,
            1,
            "answerOption1",
            SurveyServiceTestConstants.attachmentObjectKey),
        new AnswerOption(
            FullSurveyConstants.ANSWER_OPTION2_OF_QUESTION3.getId(isClone),
            question3,
            2,
            "answerOption2",
            SurveyServiceTestConstants.attachmentObjectKey));
    question3.setAnswerOptions(question3AnswerOptionList);
    surveyPage1.getQuestions().add(question1);
    surveyPage1.getQuestions().add(question2);
    surveyPage1.getQuestions().add(question3);
    survey.getPages().add(surveyPage1);

    // Заполнение ответов

    if (!isClone) {
      Response response1 = new Response(
          FullSurveyConstants.getResponse1Id(),
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
      boolean allSurveyOptionValues,
      boolean isPublished,
      Instant expireAt,
      String targetTimezone
  ) {
    return new Survey(
        SurveyServiceTestConstants.plainSurveyId,
        account1,
        PlainSurveyConstants.getTitle(otherValuesOfTitleAndDescription),
        PlainSurveyConstants.getDescription(otherValuesOfTitleAndDescription),
        allSurveyOptionValues,
        allSurveyOptionValues,
        isPublished,
        false,
        allSurveyOptionValues,
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
