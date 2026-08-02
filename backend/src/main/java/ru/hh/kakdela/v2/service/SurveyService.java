package ru.hh.kakdela.v2.service;

import java.nio.file.Paths;
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
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyService {

  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final PermissionService permissionService;
  private final NotificationService notificationService;
  private final ObjectStorageService objectStorageService;
  private final SurveyMapper surveyMapper;

  @Transactional(readOnly = true)
  public SurveyResponseDto getById(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId));

    if (!survey.isPublished()) {
      permissionService.checkHasAnyPermission(surveyId, accountId);
    }

    return surveyMapper.surveyToDto(survey);
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
        .id(UUID.randomUUID())
        .author(author)
        .title(dto.getTitle())
        .description(dto.getDescription())
        .isAuthorizedOnly(dto.getIsAuthorizedOnly())
        .isLimitedToOneResponse(dto.getIsLimitedToOneResponse())
        .doNotify(dto.getDoNotify())
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
          .atZone(ZoneId.of(survey.getTargetTimezone()))
          .toLocalDateTime()
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
        .id(UUID.randomUUID())
        .author(account)
        .title("Копия — " + originalSurvey.getTitle())
        .description(originalSurvey.getDescription())
        .isAuthorizedOnly(originalSurvey.isAuthorizedOnly())
        .isLimitedToOneResponse(originalSurvey.isLimitedToOneResponse())
        .doNotify(originalSurvey.doNotify())
        .expireAt(originalSurvey.getExpireAt())
        .targetTimezone(originalSurvey.getTargetTimezone())
        .createdAt(Instant.now().truncatedTo(ChronoUnit.SECONDS))
        .build();

    for (SurveyPage originalPage : originalSurvey.getPages()) {
      SurveyPage pageCopy = SurveyPage.builder()
          .id(UUID.randomUUID())
          .survey(surveyCopy)
          .serialNumber(originalPage.getSerialNumber())
          .title(originalPage.getTitle())
          .description(originalPage.getDescription())
          .build();

      for (Question originalQuestion : originalPage.getQuestions()) {
        UUID questionId = UUID.randomUUID();

        Question questionCopy = Question.builder()
            .id(questionId)
            .surveyPage(pageCopy)
            .serialNumber(originalQuestion.getSerialNumber())
            .text(originalQuestion.getText())
            .description(originalQuestion.getDescription())
            .type(originalQuestion.getType())
            .answerOptionOrder(originalQuestion.getAnswerOptionOrder())
            .hasOtherOption(originalQuestion.hasOtherOption())
            .isMandatory(originalQuestion.isMandatory())
            .isVisible(originalQuestion.isVisible())
            .condition(originalQuestion.getCondition())
            .build();

        if (originalQuestion.getAttachmentObjectKey() != null) {
          String questionAttachmentObjectKey =
              "questions/%s/%s".formatted(questionId, UUID.randomUUID());
          objectStorageService.copyObject(
              originalQuestion.getAttachmentObjectKey(),
              questionAttachmentObjectKey
          );
          questionCopy.setAttachmentObjectKey(questionAttachmentObjectKey);
        }

        for (AnswerOption originalOption : originalQuestion.getAnswerOptions()) {
          UUID optionId = UUID.randomUUID();

          AnswerOption optionCopy = AnswerOption.builder()
              .id(optionId)
              .question(questionCopy)
              .serialNumber(originalOption.getSerialNumber())
              .text(originalOption.getText())
              .build();

          if (originalOption.getAttachmentObjectKey() != null) {
            String optionAttachmentObjectKey =
                "answer-options/%s/%s".formatted(optionId, UUID.randomUUID());
            objectStorageService.copyObject(
                originalOption.getAttachmentObjectKey(),
                optionAttachmentObjectKey
            );
            optionCopy.setAttachmentObjectKey(optionAttachmentObjectKey);
          }
          questionCopy.getAnswerOptions().add(optionCopy);
        }

        pageCopy.getQuestions().add(questionCopy);
      }

      surveyCopy.getPages().add(pageCopy);
    }

    if (originalSurvey.getClosingPage() != null) {
      UUID closingPageId = UUID.randomUUID();

      ClosingPage closingPageCopy = ClosingPage.builder()
          .id(closingPageId)
          .survey(surveyCopy)
          .title(originalSurvey.getClosingPage().getTitle())
          .description(originalSurvey.getClosingPage().getDescription())
          .websiteUrl(originalSurvey.getClosingPage().getWebsiteUrl())
          .build();

      if (originalSurvey.getClosingPage().getAttachmentObjectKey() != null) {
        String closingAttachmentObjectKey =
            "closing-pages/%s/%s".formatted(closingPageId, UUID.randomUUID());
        objectStorageService.copyObject(
            originalSurvey.getClosingPage().getAttachmentObjectKey(),
            closingAttachmentObjectKey
        );
        closingPageCopy.setAttachmentObjectKey(closingAttachmentObjectKey);
      }

      if (originalSurvey.getClosingPage().getFileObjectKey() != null) {
        String originalFileKey = originalSurvey.getClosingPage().getFileObjectKey();
        String fileName =  Paths.get(originalFileKey).getFileName().toString();

        String closingFileObjectKey =
            "closing-pages/%s/%s".formatted(closingPageId, fileName);
        objectStorageService.copyObject(
            originalSurvey.getClosingPage().getFileObjectKey(),
            closingFileObjectKey
        );
        closingPageCopy.setFileObjectKey(closingFileObjectKey);
      }
      surveyCopy.setClosingPage(closingPageCopy);
    }

    surveyDao.save(surveyCopy);
    log.info("Клонирован опрос originalId={} copyId={} accountId={}",
        surveyId, surveyCopy.getId(), accountId);

    return surveyMapper.surveyToDto(surveyCopy);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    permissionService.checkCanDelete(id, accountId);
    Survey survey = surveyDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + id));
    surveyDao.delete(survey);
    log.info("Удален опрос id={} accountId={}", id, accountId);
  }

  // Вспомогательные методы

  private void validateAuthorizationConsistency(Survey survey) {
    if (survey.isLimitedToOneResponse() && !survey.isAuthorizedOnly()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Опция \"Запретить проходить более одного раза\" доступна только при "
              + "включённой опции \"Запретить анонимное прохождение\"");
    }
  }
}
