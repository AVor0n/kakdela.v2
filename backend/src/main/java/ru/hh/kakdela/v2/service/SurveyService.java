package ru.hh.kakdela.v2.service;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.constants.DefaultValues;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dao.SurveyNotificationSubscriptionDao;
import ru.hh.kakdela.v2.dto.image.ProcessedImage;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyPublicResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.util.JsonNullableUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyService {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final SurveyNotificationSubscriptionDao subscriptionDao;
  private final PermissionService permissionService;
  private final NotificationService notificationService;
  private final ObjectStorageService objectStorageService;
  private final ConditionService conditionService;
  private final ImageProcessingService imageProcessingService;
  private final SurveyMapper surveyMapper;

  @Transactional(readOnly = true)
  public SurveyPublicResponseDto getPublicById(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId));

    if (!survey.isPublished()) {
      permissionService.checkHasAnyPermission(surveyId, accountId);
    }

    return surveyMapper.surveyToPublicDto(
        survey, conditionService.doSurveyHaveConditions(surveyId));
  }

  @Transactional(readOnly = true)
  public SurveyResponseDto getById(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId));

    permissionService.checkHasAnyPermission(surveyId, accountId);

    return surveyMapper.surveyToDto(survey);
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseWithPermissionDto> getMySurveys(UUID accountId) {
    return permissionService.getAccessibleSurveys(accountId).stream()
        .map(surveyMapper::surveyWithRoleDtoToShortDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseDto> getMyAssignedSurveys(UUID accountId) {
    return subscriptionDao.findSurveysBySubscriberId(accountId).stream()
        .map(surveyMapper::surveyToShortDto)
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
  public SurveyResponseDto updatePartial(UUID surveyId, SurveyUpdateDto dto, UUID accountId) {
    permissionService.checkCanEdit(surveyId, accountId);

    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId));

    if (dto.getTitle().isPresent()) {
      survey.setTitle(dto.getTitle().get());
    }
    if (dto.getDescription().isPresent()) {
      survey.setDescription(dto.getDescription().get());
    }

    if (dto.getIsAuthorizedOnly().isPresent()) {
      survey.setAuthorizedOnly(
          JsonNullableUtil.ifNotNullGetOrElse(
              dto.getIsAuthorizedOnly(),
              DefaultValues.IS_AUTHORIZED_ONLY_DEFAULT));
    }
    if (dto.getIsLimitedToOneResponse().isPresent()) {
      survey.setLimitedToOneResponse(
          JsonNullableUtil.ifNotNullGetOrElse(
              dto.getIsLimitedToOneResponse(),
              DefaultValues.IS_LIMITED_TO_ONE_RESPONSE_DEFAULT));
    }
    validateAuthorizationConsistency(survey);

    final boolean wasPublished = survey.isPublished();
    if (dto.getIsPublished().isPresent()) {
      survey.setPublished(dto.getIsPublished().get());
    }

    if (dto.getDoNotify().isPresent()) {
      survey.setDoNotify(
          JsonNullableUtil.ifNotNullGetOrElse(
              dto.getDoNotify(),
              DefaultValues.DO_NOTIFY_DEFAULT));
    }

    if (dto.getExpireAtAtTargetTimezone().isPresent()) {
      String targetTimezone = JsonNullableUtil.ifNotNullGetOrElseIfNullFirstOrElseSecond(
          dto.getTargetTimezone(),
          DefaultValues.TARGET_TIMEZONE_DEFAULT,
          survey.getTargetTimezone());

      if (dto.getExpireAtAtTargetTimezone().get() != null) {
        Instant expireAt = dto.getExpireAtAtTargetTimezone().get()
            .atZone(ZoneId.of(targetTimezone))
            .toInstant()
            .truncatedTo(ChronoUnit.SECONDS);

        validateExpireAt(expireAt);
        survey.setExpireAt(expireAt);
      } else {
        survey.setExpireAt(null);
      }

      if (dto.getTargetTimezone().isPresent()) {
        survey.setTargetTimezone(targetTimezone);
      }
    } else if (dto.getTargetTimezone().isPresent()) {
      String targetTimezone = JsonNullableUtil.ifNotNullGetOrElse(
          dto.getTargetTimezone(),
          DefaultValues.TARGET_TIMEZONE_DEFAULT);

      if (survey.getExpireAt() != null) {
        Instant expireAt = survey.getExpireAt()
            .atZone(ZoneId.of(survey.getTargetTimezone()))
            .toLocalDateTime()
            .atZone(ZoneId.of(targetTimezone))
            .toInstant()
            .truncatedTo(ChronoUnit.SECONDS);

        validateExpireAtAfterTargetTimezoneChanged(expireAt);
        survey.setExpireAt(expireAt);
      }

      survey.setTargetTimezone(targetTimezone);
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

  // Attachment management

  @Transactional
  public ObjectUrlResponseDto addAttachment(UUID surveyId, UUID accountId, MultipartFile file) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    permissionService.checkCanEdit(survey.getId(), accountId);

    if (survey.getAttachmentObjectKey() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "К опросу уже прикреплено вложение");
    }

    ProcessedImage image = imageProcessingService.process(file);

    String objectKey = "opening-pages/%s/%s".formatted(surveyId, UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    survey.setAttachmentObjectKey(objectKey);
    surveyDao.update(survey);
    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public ObjectUrlResponseDto updateAttachment(UUID surveyId,
                                               UUID accountId,
                                               MultipartFile file) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    permissionService.checkCanEdit(survey.getId(), accountId);

    ProcessedImage image = imageProcessingService.process(file);

    if (survey.getAttachmentObjectKey() != null) {
      objectStorageService.deleteObject(
          survey.getAttachmentObjectKey());
    }

    String objectKey = "opening-pages/%s/%s".formatted(surveyId, UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    survey.setAttachmentObjectKey(objectKey);
    surveyDao.update(survey);
    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public void deleteAttachment(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    permissionService.checkCanEdit(survey.getId(), accountId);

    if (survey.getAttachmentObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Опрос не содержит вложения");
    }

    objectStorageService.deleteObject(survey.getAttachmentObjectKey());

    survey.setAttachmentObjectKey(null);
    surveyDao.update(survey);
  }

  // Вспомогательные методы

  private void validateAuthorizationConsistency(Survey survey) {
    if (survey.isLimitedToOneResponse() && !survey.isAuthorizedOnly()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Опция \"Запретить проходить более одного раза\" доступна только при "
              + "включённой опции \"Запретить анонимное прохождение\"");
    }
  }

  private void validateExpireAt(Instant expireAt) {
    if (expireAt.isBefore(Instant.now())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Дедлайн не может быть в прошлом");
    }
  }

  private void validateExpireAtAfterTargetTimezoneChanged(Instant expireAt) {
    if (expireAt.isBefore(Instant.now())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Дедлайн в указанном часовом поясе уже прошёл");
    }
  }
}
