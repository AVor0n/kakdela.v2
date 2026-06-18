package ru.hh.kakdela_v2.util;

import java.time.Duration;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hh.kakdela_v2.dto.account.AccountResponseDto;
import ru.hh.kakdela_v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.dto.closing_page.ClosingPageResponseDto;
import ru.hh.kakdela_v2.dto.permission.PermissionResponseDto;
import ru.hh.kakdela_v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela_v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageResponseDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Answer;
import ru.hh.kakdela_v2.model.AnswerOption;
import ru.hh.kakdela_v2.model.ClosingPage;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.model.Response;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyPage;
import ru.hh.kakdela_v2.service.ObjectStorageService;

@Service
@RequiredArgsConstructor
public class MapperUtil {

  private final ObjectStorageService objectStorageService;

  public AccountResponseDto accountToDto(Account account) {
    return new AccountResponseDto(
        account.getId(),
        account.getLogin(),
        account.getEmail(),
        account.getRegisteredAt()
    );
  }

  public AnswerResponseDto answerToDto(Answer answer) {
    return new AnswerResponseDto(
        answer.getId().getResponseId(),
        answer.getId().getQuestionId(),
        answer.getAnswerText()
    );
  }

  public AnswerOptionResponseDto answerOptionToDto(AnswerOption answerOption) {
    String attachmentUrl = objectStorageService.generateObjectUrl(
        answerOption.getAttachmentObjectKey(),
        Duration.ofMinutes(1)
    ).toString();

    return new AnswerOptionResponseDto(
        answerOption.getId(),
        answerOption.getSerialNumber(),
        answerOption.getAnswerOptionText()
    );
  }

  public ClosingPageResponseDto closingPageToDto(ClosingPage closingPage) {
    return new ClosingPageResponseDto(
        closingPage.getTitle(),
        closingPage.getDescription(),
        closingPage.getWebsiteUrl()
    );
  }

  public PermissionResponseDto permissionToDto(Permission permission) {
    return new PermissionResponseDto(
        permission.getId().getAccountId(),
        permission.getId().getSurveyId(),
        permission.getRole().name(),
        permission.isDoNotify()
    );
  }

  public QuestionResponseDto questionToDto(Question question) {
    String attachmentUrl = objectStorageService.generateObjectUrl(
        question.getAttachmentObjectKey(),
        Duration.ofMinutes(1)
    ).toString();

    return new QuestionResponseDto(
        question.getId(),
        question.getSerialNumber(),
        question.getTitle(),
        question.getDescription(),
        question.getType().name(),
        question.getAnswerOptionOrder() != null
            ? question.getAnswerOptionOrder().name()
            : null,
        question.isMandatory(),
        question.isVisible(),
        question.getCondition(),
        question.getAnswerOptions().stream()
            .sorted(Comparator.comparingInt(AnswerOption::getSerialNumber))
            .map(this::answerOptionToDto)
            .toList()
    );
  }

  public ResponseResponseDto responseToDto(Response response) {
    return new ResponseResponseDto(
        response.getId(),
        response.getAccount() != null
            ? response.getAccount().getId()
            : null,
        response.getSurvey().getId(),
        response.isCompleted(),
        response.getReceivedAt(),
        response.getAnswers().stream()
            .map(this::answerToDto)
            .toList()
    );
  }

  public SurveyResponseDto surveyToDto(Survey survey) {
    return new SurveyResponseDto(
        survey.getId(),
        survey.getAuthor().getId(),
        survey.getTitle(),
        survey.getDescription(),
        survey.isAuthorizedOnly(),
        survey.isLimitedToOneResponse(),
        survey.isPublished(),
        survey.isTemplate(),
        survey.isDoNotify(),
        survey.getExpireAt(),
        survey.getCreatedAt(),
        survey.getPages().stream()
            .sorted(Comparator.comparingInt(SurveyPage::getSerialNumber))
            .map(this::surveyPageToDto)
            .toList(),
        survey.getClosingPage() != null
            ? new ClosingPageResponseDto(survey.getClosingPage())
            : null
    );
  }

  public SurveyShortResponseDto surveyToShortDto(Survey survey) {
    return new SurveyShortResponseDto(
        survey.getId(),
        survey.getTitle(),
        survey.getDescription(),
        survey.isPublished(),
        survey.getCreatedAt()
    );
  }

  public SurveyPageResponseDto surveyPageToDto(SurveyPage surveyPage) {
    return new SurveyPageResponseDto(
        surveyPage.getId(),
        surveyPage.getSurvey().getId(),
        surveyPage.getSerialNumber(),
        surveyPage.getTitle(),
        surveyPage.getDescription(),
        surveyPage.getQuestions().stream()
            .sorted(Comparator.comparingInt(Question::getSerialNumber))
            .map(this::questionToDto)
            .toList()
    );
  }
}
