package ru.hh.kakdela.v2.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.ClosingPageDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.closing_page.ClosingPageCreateDto;
import ru.hh.kakdela.v2.dto.closing_page.ClosingPageResponseDto;
import ru.hh.kakdela.v2.dto.closing_page.ClosingPageUpdateDto;
import ru.hh.kakdela.v2.dto.image.ProcessedImage;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Survey;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosingPageService {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final ClosingPageDao closingPageDao;
  private final SurveyDao surveyDao;
  private final PermissionService permissionService;
  private final ObjectStorageService objectStorageService;
  private final ImageProcessingService imageProcessingService;

  @Transactional(readOnly = true)
  public ClosingPageResponseDto getBySurveyId(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    return mapToResponseDto(closingPage);
  }

  @Transactional(readOnly = true)
  public boolean existsBySurveyId(UUID surveyId, UUID accountId) {
    return closingPageDao.existsBySurveyId(surveyId);
  }

  @Transactional
  public ClosingPageResponseDto create(UUID surveyId, ClosingPageCreateDto dto, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    permissionService.checkCanEdit(survey.getId(), accountId);

    if (closingPageDao.existsBySurveyId(surveyId)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Завершающая страница уже существует для опроса: " + surveyId);
    }

    ClosingPage closingPage = ClosingPage.builder()
        .id(surveyId)
        .survey(survey)
        .title(dto.getTitle())
        .description(dto.getDescription())
        .websiteUrl(dto.getWebsiteUrl())
        .build();

    closingPageDao.save(closingPage);
    return mapToResponseDto(closingPage);
  }

  @Transactional
  public ClosingPageResponseDto update(UUID surveyId, ClosingPageUpdateDto dto, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    permissionService.checkCanEdit(survey.getId(), accountId);

    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    if (dto.getTitle() != null) {
      closingPage.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      closingPage.setDescription(dto.getDescription());
    }
    if (dto.getWebsiteUrl() != null) {
      closingPage.setWebsiteUrl(dto.getWebsiteUrl());
    }
    closingPageDao.update(closingPage);
    return mapToResponseDto(closingPage);
  }

  @Transactional
  public void delete(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    permissionService.checkCanEdit(survey.getId(), accountId);

    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    if (closingPage.getAttachmentObjectKey() != null) {
      objectStorageService.deleteObject(closingPage.getAttachmentObjectKey());
    }
    closingPageDao.delete(closingPage);
  }

  // Attachment management

  @Transactional
  public ObjectUrlResponseDto addAttachment(UUID surveyId, UUID accountId, MultipartFile file) {
    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    permissionService.checkCanEdit(closingPage.getSurvey().getId(), accountId);

    if (closingPage.getAttachmentObjectKey() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "К завершающей странице уже прикреплено вложение");
    }

    ProcessedImage image = imageProcessingService.process(file);

    String objectKey = "closing/%s/%s".formatted(surveyId, UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    closingPage.setAttachmentObjectKey(objectKey);
    closingPageDao.update(closingPage);
    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public ObjectUrlResponseDto updateAttachment(UUID surveyId,
                                               UUID accountId,
                                               MultipartFile file) {
    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    permissionService.checkCanEdit(closingPage.getSurvey().getId(), accountId);

    ProcessedImage image = imageProcessingService.process(file);

    if (closingPage.getAttachmentObjectKey() != null) {
      objectStorageService.deleteObject(
          closingPage.getAttachmentObjectKey());
    }

    String objectKey = "closing/%s/%s".formatted(surveyId, UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    closingPage.setAttachmentObjectKey(objectKey);
    closingPageDao.update(closingPage);
    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public void deleteAttachment(UUID surveyId, UUID accountId) {
    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    permissionService.checkCanEdit(closingPage.getSurvey().getId(), accountId);

    if (closingPage.getAttachmentObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Завершающая страница не содержит вложения");
    }

    objectStorageService.deleteObject(closingPage.getAttachmentObjectKey());

    closingPage.setAttachmentObjectKey(null);
    closingPageDao.update(closingPage);
  }

  private ClosingPageResponseDto mapToResponseDto(ClosingPage closingPage) {
    String attachmentUrl = closingPage.getAttachmentObjectKey() != null
        ? objectStorageService.generateObjectUrl(
        closingPage.getAttachmentObjectKey(),
        attachmentUrlMaxAge
    ).toString()
        : null;

    return new ClosingPageResponseDto(
        closingPage.getTitle(),
        closingPage.getDescription(),
        attachmentUrl,
        closingPage.getWebsiteUrl()
    );
  }
}
