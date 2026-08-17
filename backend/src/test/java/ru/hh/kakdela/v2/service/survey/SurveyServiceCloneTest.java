package ru.hh.kakdela.v2.service.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants.FullSurveyConstants;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestEntity;
import ru.hh.kakdela.v2.util.service.survey.SurveyServiceTestConstants;

public class SurveyServiceCloneTest extends SurveyServiceTestBase {

  private final String attachmentObjectKeyRegexEnd = "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

  @Test
  void clone_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.clone(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account2Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + FullSurveyConstants.SURVEY.getId(IS_ORIGINAL) + "\"",
        ex.getMessage()
    );
  }

  @Test
  void clone_accountNotFound_throwException() {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.of(SurveyServiceTestEntity.getFullSurvey(true, false)));
    Mockito.when(accountDao.findById(SurveyServiceTestConstants.account2Id))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.clone(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account2Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Аккаунт не найден: " + SurveyServiceTestConstants.account2Id + "\"",
        ex.getMessage()
    );
  }

  @Test
  void clone_surveyFound_checkPermissions() throws MalformedURLException {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.of(fullSurvey));
    Mockito.when(accountDao.findById(SurveyServiceTestConstants.account2Id))
        .thenReturn(Optional.of(SurveyServiceTestEntity.account2));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    surveyService.clone(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account2Id);

    Mockito.verify(permissionService)
        .checkCanEdit(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account2Id);
  }

  @Test
  void clone_surveyWithClosingPage_createCorrectCloneEntity() throws MalformedURLException {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.of(fullSurvey));
    Mockito.when(accountDao.findById(SurveyServiceTestConstants.account2Id))
        .thenReturn(Optional.of(SurveyServiceTestEntity.account2));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());
    Mockito.when(objectStorageService.getFileSize(Mockito.anyString()))
        .thenReturn(102400L);

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertNotNull(survey.getId(), "ID опроса не был заполнен");
      assertTrue(survey.getId().toString().matches(uuidRegex), "ID не является UUID");

      survey.setId(FullSurveyConstants.SURVEY.getId(IS_CLONE));

      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
              && !survey.getCreatedAt().isAfter(Instant.now()),
          "Установленное время создания оказалось в будущем или было слишком давно");
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000,
          "Время создания должно иметь обрезанные миллисекунды и наносекунды");

      survey.setCreatedAt(null);

      assertTrue(survey.getTitle().matches("^<p>Копия — .*$"),
          "Копия опроса должна иметь префикс \"Копия —\" в названии");
      assertFalse(survey.isPublished(),
          "Копия опроса не должна быть опубликована");
      assertEquals(Collections.emptyList(), survey.getResponses(),
          "Копия опроса не должна иметь ответов");
      assertEquals(Collections.emptyList(), survey.getPermissions(),
          "Копия опроса не должна иметь настроенных прав");

      SurveyPage page = survey.getPages().getFirst();

      assertNotNull(page.getId(), "ID страницы не был заполнен");
      assertTrue(page.getId().toString().matches(uuidRegex), "ID не является UUID");

      page.setId(FullSurveyConstants.PAGE1.getId(IS_CLONE));

      assertNotNull(page.getQuestions().getFirst().getId(), "ID вопроса не был заполнен");
      assertTrue(page.getQuestions().getFirst().getId().toString().matches(uuidRegex), "ID не является UUID");

      assertNotNull(page.getQuestions().getFirst().getAttachmentObjectKey(),
          "Ключ картинки, прикреплённой к вопросу, не был заполнен");
      assertTrue(page.getQuestions().getFirst().getAttachmentObjectKey()
              .matches("^questions/" + page.getQuestions().getFirst().getId() + attachmentObjectKeyRegexEnd),
          "Ключ картинки, прикреплённой к вопросу, не соответствует требуемому формату");

      page.getQuestions().getFirst().setAttachmentObjectKey("attachmentObjectKey");
      page.getQuestions().getFirst().setId(FullSurveyConstants.QUESTION1.getId(IS_CLONE));

      Question question2 = page.getQuestions().get(1);
      page.getQuestions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question2.setId(FullSurveyConstants.QUESTION2.getId(IS_CLONE));

      assertNotNull(question2.getAnswerOptions().getFirst().getId(), "ID варианта ответа не был заполнен");
      assertTrue(question2.getAnswerOptions().getFirst().getId().toString().matches(uuidRegex), "ID не является UUID");

      assertNotNull(question2.getAnswerOptions().getFirst().getAttachmentObjectKey(),
          "Ключ картинки, прикреплённой к варианту ответа, не был заполнен");
      assertTrue(question2.getAnswerOptions().getFirst().getAttachmentObjectKey()
              .matches("^answer-options/" + question2.getAnswerOptions().getFirst().getId() + attachmentObjectKeyRegexEnd),
          "Ключ картинки, прикреплённой к варианту ответа, не соответствует требуемому формату");

      question2.getAnswerOptions().get(0).setAttachmentObjectKey("attachmentObjectKey");
      question2.getAnswerOptions().get(0).setId(FullSurveyConstants.ANSWER_OPTION1_OF_QUESTION2.getId(IS_CLONE));
      question2.getAnswerOptions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question2.getAnswerOptions().get(1).setId(FullSurveyConstants.ANSWER_OPTION2_OF_QUESTION2.getId(IS_CLONE));

      Question question3 = page.getQuestions().get(2);
      page.getQuestions().get(2).setAttachmentObjectKey("attachmentObjectKey");
      question3.setId(FullSurveyConstants.QUESTION3.getId(IS_CLONE));
      question3.getAnswerOptions().get(0).setAttachmentObjectKey("attachmentObjectKey");
      question3.getAnswerOptions().get(0).setId(FullSurveyConstants.ANSWER_OPTION1_OF_QUESTION3.getId(IS_CLONE));
      question3.getAnswerOptions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question3.getAnswerOptions().get(1).setId(FullSurveyConstants.ANSWER_OPTION2_OF_QUESTION3.getId(IS_CLONE));

      ClosingPage closingPage = survey.getClosingPage();
      assertNotNull(closingPage);
      assertNotNull(closingPage.getId());
      assertTrue(closingPage.getId().toString().matches(uuidRegex));

      assertNotNull(closingPage.getFileObjectKey());
      assertTrue(closingPage.getFileObjectKey()
          .matches("^closing-pages/[0-9a-fA-F-]+/[^/]+$"));

      closingPage.setFileObjectKey(SurveyServiceTestConstants.fileObjectKey);
      closingPage.setAttachmentObjectKey("attachmentObjectKey");
      closingPage.setId(FullSurveyConstants.CLOSING_PAGE.getId(IS_CLONE));

      assertEquals(fullSurveyClone, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.clone
        (FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account2Id);
    assertEquals(fullSurveyCloneResponseDto, result);

    Mockito.verify(
        objectStorageService, times(9)).copyObject(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void clone_surveyWithoutClosingPage_createCorrectCloneEntity() throws MalformedURLException {
    Mockito.when(surveyDao.findById(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL)))
        .thenReturn(Optional.of(fullSurveyWithoutClosingPage));
    Mockito.when(accountDao.findById(SurveyServiceTestConstants.account2Id))
        .thenReturn(Optional.of(SurveyServiceTestEntity.account2));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertNotNull(survey.getId(), "ID опроса не был заполнен");
      assertTrue(survey.getId().toString().matches(uuidRegex), "ID не является UUID");

      survey.setId(FullSurveyConstants.SURVEY.getId(IS_CLONE));

      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
              && !survey.getCreatedAt().isAfter(Instant.now()),
          "Установленное время создания оказалось в будущем или было слишком давно");
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000,
          "Время создания должно иметь обрезанные миллисекунды и наносекунды");

      survey.setCreatedAt(null);

      assertTrue(survey.getTitle().matches("^<p>Копия — .*$"),
          "Копия опроса должна иметь префикс \"Копия —\" в названии");
      assertFalse(survey.isPublished(), "Копия опроса не должна быть опубликована");
      assertEquals(Collections.emptyList(), survey.getResponses(),
          "Копия опроса не должна иметь ответов");
      assertEquals(Collections.emptyList(), survey.getPermissions(),
          "Копия опроса не должна иметь настроенных прав");
      assertNull(survey.getClosingPage(),
          "Копия опроса без завершающей страницы не должна иметь её");

      SurveyPage page = survey.getPages().getFirst();

      assertNotNull(page.getId(), "ID страницы не был заполнен");
      assertTrue(page.getId().toString().matches(uuidRegex), "ID не является UUID");

      page.setId(FullSurveyConstants.PAGE1.getId(IS_CLONE));

      assertNotNull(page.getQuestions().getFirst().getId(), "ID вопроса не был заполнен");
      assertTrue(page.getQuestions().getFirst().getId().toString().matches(uuidRegex), "ID не является UUID");

      assertNotNull(page.getQuestions().getFirst().getAttachmentObjectKey(),
          "Ключ картинки, прикреплённой к вопросу, не был заполнен");
      assertTrue(page.getQuestions().getFirst().getAttachmentObjectKey()
              .matches("^questions/" + page.getQuestions().getFirst().getId() + attachmentObjectKeyRegexEnd),
          "Ключ картинки, прикреплённой к вопросу, не соответствует требуемому формату");

      page.getQuestions().getFirst().setAttachmentObjectKey("attachmentObjectKey");
      page.getQuestions().getFirst().setId(FullSurveyConstants.QUESTION1.getId(IS_CLONE));

      Question question2 = page.getQuestions().get(1);
      page.getQuestions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question2.setId(FullSurveyConstants.QUESTION2.getId(IS_CLONE));

      assertNotNull(question2.getAnswerOptions().getFirst().getId(), "ID варианта ответа не был заполнен");
      assertTrue(question2.getAnswerOptions().getFirst().getId().toString().matches(uuidRegex), "ID не является UUID");

      assertNotNull(question2.getAnswerOptions().getFirst().getAttachmentObjectKey(),
          "Ключ картинки, прикреплённой к варианту ответа, не был заполнен");
      assertTrue(question2.getAnswerOptions().getFirst().getAttachmentObjectKey()
              .matches("^answer-options/" + question2.getAnswerOptions().getFirst().getId() + attachmentObjectKeyRegexEnd),
          "Ключ картинки, прикреплённой к варианту ответа, не соответствует требуемому формату");

      question2.getAnswerOptions().get(0).setAttachmentObjectKey("attachmentObjectKey");
      question2.getAnswerOptions().get(0).setId(FullSurveyConstants.ANSWER_OPTION1_OF_QUESTION2.getId(IS_CLONE));
      question2.getAnswerOptions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question2.getAnswerOptions().get(1).setId(FullSurveyConstants.ANSWER_OPTION2_OF_QUESTION2.getId(IS_CLONE));

      Question question3 = page.getQuestions().get(2);
      page.getQuestions().get(2).setAttachmentObjectKey("attachmentObjectKey");
      question3.setId(FullSurveyConstants.QUESTION3.getId(IS_CLONE));
      question3.getAnswerOptions().get(0).setAttachmentObjectKey("attachmentObjectKey");
      question3.getAnswerOptions().get(0).setId(FullSurveyConstants.ANSWER_OPTION1_OF_QUESTION3.getId(IS_CLONE));
      question3.getAnswerOptions().get(1).setAttachmentObjectKey("attachmentObjectKey");
      question3.getAnswerOptions().get(1).setId(FullSurveyConstants.ANSWER_OPTION2_OF_QUESTION3.getId(IS_CLONE));

      assertEquals(fullSurveyCloneWithoutClosingPage, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result =
        surveyService.clone(FullSurveyConstants.SURVEY.getId(IS_ORIGINAL), SurveyServiceTestConstants.account2Id);
    assertEquals(fullSurveyCloneWithoutClosingPageResponseDto, result);

    Mockito.verify(
        objectStorageService, times(7)).copyObject(Mockito.anyString(), Mockito.anyString());
  }
}
