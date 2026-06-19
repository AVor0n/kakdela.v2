package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela_v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.AnswerOption;
import ru.hh.kakdela_v2.model.ClosingPage;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyPage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final PermissionService permissionService;

  @Transactional(readOnly = true)
  public SurveyResponseDto getById(UUID id) {
    Survey survey = surveyDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + id));
    return new SurveyResponseDto(survey);
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseDto> getAllByAuthorId(UUID authorId) {
    return surveyDao.findAllByAuthorId(authorId).stream()
            .map(SurveyShortResponseDto::new)
            .toList();
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseDto> getMySurveys(UUID accountId) {
    List<Survey> surveys = permissionService.getAccessibleSurveys(accountId);
    return surveys.stream()
        .map(SurveyShortResponseDto::new)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseDto> getAllPublished() {
    return surveyDao.findAllPublished().stream()
            .map(SurveyShortResponseDto::new)
            .toList();
  }

  @Transactional
  public SurveyResponseDto create(UUID authorId, SurveyCreateDto dto) {
    Account author = accountDao.findById(authorId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Аккаунт не найден: " + authorId));

    Survey survey = Survey.builder()
        .author(author)
        .title(dto.getTitle())
        .description(dto.getDescription())
        .isAuthorizedOnly(dto.getIsAuthorizedOnly())
        .isLimitedToOneResponse(dto.getIsLimitedToOneResponse())
        .doNotify(dto.getDoNotify())
        .isPublished(false)
        .isTemplate(false)
        .expireAt(dto.getExpireAt())
        .createdAt(Instant.now())
        .build();

    surveyDao.save(survey);
    return new SurveyResponseDto(survey);
  }

  @Transactional
  public SurveyResponseDto update(UUID surveyId, SurveyUpdateDto dto, UUID accountId) {
    permissionService.checkAccess(surveyId, accountId, SurveyRole.EDITOR);
    Survey survey = surveyDao.findById(surveyId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    if (dto.getTitle() != null) survey.setTitle(dto.getTitle());
    if (dto.getDescription() != null) survey.setDescription(dto.getDescription());
    if (dto.getIsAuthorizedOnly() != null) survey.setAuthorizedOnly(dto.getIsAuthorizedOnly());
    if (dto.getIsLimitedToOneResponse() != null) survey.setLimitedToOneResponse(dto.getIsLimitedToOneResponse());
    if (dto.getIsPublished() != null) survey.setPublished(dto.getIsPublished());
    if (dto.getDoNotify() != null) survey.setDoNotify(dto.getDoNotify());
    if (dto.getExpireAt() != null) survey.setExpireAt(dto.getExpireAt());

    surveyDao.update(survey);
    return new SurveyResponseDto(survey);
  }

  @Transactional
  public SurveyResponseDto clone(UUID surveyId, UUID accountId) {
    Survey originalSurvey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    permissionService.checkAccess(surveyId, accountId, SurveyRole.EDITOR);

    Account account = accountDao.findById(accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден: " + accountId));

    Survey surveyCopy = Survey.builder()
        .author(account)
        .title("Копия — " + originalSurvey.getTitle())
        .description(originalSurvey.getDescription())
        .isAuthorizedOnly(originalSurvey.isAuthorizedOnly())
        .isLimitedToOneResponse(originalSurvey.isLimitedToOneResponse())
        .isPublished(false)
        .isTemplate(false)
        .doNotify(originalSurvey.isDoNotify())
        .expireAt(originalSurvey.getExpireAt())
        .createdAt(Instant.now())
        .build();

    for (SurveyPage originalPage : originalSurvey.getPages()) {
      SurveyPage pageCopy = SurveyPage.builder()
          .survey(surveyCopy)
          .serialNumber(originalPage.getSerialNumber())
          .title(originalPage.getTitle())
          .description(originalPage.getDescription())
          .build();

      for (Question originalQuestion : originalPage.getQuestions()) {
        Question questionCopy = Question.builder()
            .surveyPage(pageCopy)
            .serialNumber(originalQuestion.getSerialNumber())
            .title(originalQuestion.getTitle())
            .description(originalQuestion.getDescription())
            .type(originalQuestion.getType())
            .answerOptionOrder(originalQuestion.getAnswerOptionOrder())
            .isMandatory(originalQuestion.isMandatory())
            .isVisible(originalQuestion.isVisible())
            .condition(originalQuestion.getCondition())
            .build();

        for (AnswerOption originalOption : originalQuestion.getAnswerOptions()) {
          AnswerOption optionCopy = AnswerOption.builder()
              .question(questionCopy)
              .serialNumber(originalOption.getSerialNumber())
              .answerOptionText(originalOption.getAnswerOptionText())
              .build();
          questionCopy.getAnswerOptions().add(optionCopy);
        }

        pageCopy.getQuestions().add(questionCopy);
      }

      surveyCopy.getPages().add(pageCopy);
    }

    if (originalSurvey.getClosingPage() != null) {
      ClosingPage closingPageCopy = ClosingPage.builder()
          .survey(surveyCopy)
          .title(originalSurvey.getClosingPage().getTitle())
          .description(originalSurvey.getClosingPage().getDescription())
          .websiteUrl(originalSurvey.getClosingPage().getWebsiteUrl())
          .build();
      surveyCopy.setClosingPage(closingPageCopy);
    }

    surveyDao.save(surveyCopy);
    return new SurveyResponseDto(surveyCopy);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    permissionService.checkOwnership(id, accountId);
    Survey survey = surveyDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + id));
    surveyDao.delete(survey);
  }
}
