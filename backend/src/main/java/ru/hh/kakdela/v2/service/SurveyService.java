package ru.hh.kakdela.v2.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.PermissionDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Permission.SurveyRole;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyService {

  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final PermissionDao permissionDao;
  private final PermissionService permissionService;
  private final NotificationService notificationService;
  private final SurveyMapper surveyMapper;

  private void validateAuthorizationConsistency(Survey survey) {
    if (survey.isLimitedToOneResponse() && !survey.isAuthorizedOnly()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Опция \"Запретить проходить более одного раза\" доступна только при "
              + "включённой опции \"Запретить анонимное прохождение\"");
    }
  }

  @Transactional(readOnly = true)
  public SurveyResponseDto getById(UUID id) {
    Survey survey = surveyDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + id));
    return surveyMapper.surveyToDto(survey);
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseWithPermissionDto> getAllByAuthorId(UUID authorId) {
    return surveyDao.findAllByAuthorId(authorId).stream()
        .map(survey -> {
          return surveyMapper.surveyToShortDto(survey, SurveyRole.AUTHOR);
        })
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseWithPermissionDto> getMySurveys(UUID accountId) {
    return permissionService.getAccessibleSurveys(accountId).stream()
        .map(surveyMapper::surveyWithRoleDtoToShortDto)
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
        .expireAt(dto.getExpireAtAtTargetTimezone() != null
            ? dto.getExpireAtAtTargetTimezone()
            .atZone(ZoneId.of(dto.getTargetTimezone()))
            .toInstant()
            .truncatedTo(ChronoUnit.SECONDS)
            : null)
        .targetTimezone(dto.getTargetTimezone())
        .createdAt(Instant.now().truncatedTo(ChronoUnit.SECONDS))
        .build();

    validateAuthorizationConsistency(survey);

    surveyDao.save(survey);
    log.info("Создан опрос id={} authorId={}", survey.getId(), authorId);
    return surveyMapper.surveyToDto(survey);
  }

  @Transactional
  public SurveyResponseDto update(UUID surveyId, SurveyUpdateDto dto, UUID accountId) {
    permissionService.checkCanEdit(surveyId, accountId);

    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    if (dto.getTitle() != null) {
      survey.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      survey.setDescription(dto.getDescription());
    }
    if (dto.getIsAuthorizedOnly() != null) {
      survey.setAuthorizedOnly(dto.getIsAuthorizedOnly());
    }
    if (dto.getIsLimitedToOneResponse() != null) {
      survey.setLimitedToOneResponse(dto.getIsLimitedToOneResponse());
    }
    validateAuthorizationConsistency(survey);

    final boolean wasPublished = survey.isPublished();
    if (dto.getIsPublished() != null) {
      survey.setPublished(dto.getIsPublished());
    }
    if (dto.getDoNotify() != null) {
      survey.setDoNotify(dto.getDoNotify());
    }
    if (dto.getExpireAtAtTargetTimezone() != null) {
      if (dto.getTargetTimezone() != null) {
        survey.setExpireAt(dto.getExpireAtAtTargetTimezone()
            .atZone(ZoneId.of(dto.getTargetTimezone()))
            .toInstant()
            .truncatedTo(ChronoUnit.SECONDS));
        survey.setTargetTimezone(dto.getTargetTimezone());
      } else {
        survey.setExpireAt(dto.getExpireAtAtTargetTimezone()
            .atZone(ZoneId.of(survey.getTargetTimezone()))
            .toInstant()
            .truncatedTo(ChronoUnit.SECONDS));
      }
    } else if (dto.getTargetTimezone() != null) {
      survey.setExpireAt(survey.getExpireAt()
          .atZone(ZoneId.of(dto.getTargetTimezone()))
          .toInstant()
          .truncatedTo(ChronoUnit.SECONDS));
      survey.setTargetTimezone(dto.getTargetTimezone());
    }

    surveyDao.update(survey);
    log.info("Изменен опрос id={} accountId={}", surveyId, accountId);

    if (survey.isPublished() && !wasPublished) {
      notificationService.sendSurveyPublishedNotifications(surveyId);
    }
    return surveyMapper.surveyToDto(survey);
  }

  @Transactional
  public SurveyResponseDto clone(UUID surveyId, UUID accountId) {
    Survey originalSurvey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    permissionService.checkCanEdit(surveyId, accountId);

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
    log.info("Клонирован опрос originalId={} copyId={} accountId={}",
        surveyId, surveyCopy.getId(), accountId);
    return surveyMapper.surveyToDto(surveyCopy);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    permissionService.checkOwnership(id, accountId);
    Survey survey = surveyDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + id));
    surveyDao.delete(survey);
    log.info("Удален опрос id={} accountId={}", id, accountId);
  }

}
