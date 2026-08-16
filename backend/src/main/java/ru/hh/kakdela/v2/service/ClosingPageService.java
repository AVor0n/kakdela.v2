package ru.hh.kakdela.v2.service;

import java.io.IOException;
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
import ru.hh.kakdela.v2.dto.closing.ClosingPageCreateDto;
import ru.hh.kakdela.v2.dto.closing.ClosingPageResponseDto;
import ru.hh.kakdela.v2.dto.closing.ClosingPageUpdateDto;
import ru.hh.kakdela.v2.dto.file.FileResponseDto;
import ru.hh.kakdela.v2.dto.image.ProcessedImage;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.mapper.ClosingPageMapper;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.util.DataConstraintUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosingPageService {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  @Value("${app.files.max-size:10485760}")
  private long maxFileSize;

  private final ClosingPageDao closingPageDao;
  private final SurveyDao surveyDao;
  private final PermissionService permissionService;
  private final ResponseService responseService;
  private final ObjectStorageService objectStorageService;
  private final ImageProcessingService imageProcessingService;
  private final ClosingPageMapper closingPageMapper;

  @Transactional(readOnly = true)
  public ClosingPageResponseDto getPublicBySurveyId(
      UUID surveyId,
      UUID responseId,
      UUID accountId,
      String token
  ) {
    if (!surveyDao.existsById(surveyId)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId);
    }

    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    Response response =
        responseService.getEntityByIdWithOwnerAccessCheck(responseId, accountId, token);

    if (!response.getSurvey().getId().equals(surveyId)
        || !response.isCompleted()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Доступ к странице запрещён");
    }

    return closingPageMapper.closingPageToDto(closingPage);
  }

  @Transactional(readOnly = true)
  public ClosingPageResponseDto getBySurveyId(UUID surveyId, UUID accountId) {
    if (!surveyDao.existsById(surveyId)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId);
    }

    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    permissionService.checkHasAnyPermission(surveyId, accountId);

    return closingPageMapper.closingPageToDto(closingPage);
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

    DataConstraintUtil.checkTitleLength(dto.getTitle());
    DataConstraintUtil.checkDescriptionLength(dto.getDescription());

    ClosingPage closingPage = ClosingPage.builder()
        .id(surveyId)
        .survey(survey)
        .title(dto.getTitle())
        .description(dto.getDescription())
        .websiteUrl(dto.getWebsiteUrl())
        .build();

    closingPageDao.save(closingPage);
    return closingPageMapper.closingPageToDto(closingPage);
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
      DataConstraintUtil.checkTitleLength(dto.getTitle());
      closingPage.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      DataConstraintUtil.checkDescriptionLength(dto.getDescription());
      closingPage.setDescription(dto.getDescription());
    }
    if (dto.getWebsiteUrl() != null) {
      closingPage.setWebsiteUrl(dto.getWebsiteUrl());
    }
    closingPageDao.update(closingPage);
    return closingPageMapper.closingPageToDto(closingPage);
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

    if (closingPage.getFileObjectKey() != null) {
      objectStorageService.deleteObject(closingPage.getFileObjectKey());
    }

    closingPageDao.deleteBySurveyId(surveyId);

    log.info("Удалена завершающая страница для опроса id={}", surveyId);
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

    String objectKey = "closing-pages/%s/%s".formatted(surveyId, UUID.randomUUID());
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

    String objectKey = "closing-pages/%s/%s".formatted(surveyId, UUID.randomUUID());
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

  @Transactional
  public FileResponseDto addFile(UUID surveyId, UUID accountId, MultipartFile file) {
    validateFile(file);

    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    permissionService.checkCanEdit(closingPage.getSurvey().getId(), accountId);

    if (closingPage.getFileObjectKey() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "К завершающей странице уже прикреплен файл");
    }

    byte[] fileBytes = getFileBytes(file);

    String clearFileName = getClearFileName(file);

    String objectKey = "closing-pages/%s/%s".formatted(surveyId, clearFileName);
    objectStorageService.putObject(objectKey, fileBytes, file.getContentType());

    closingPage.setFileObjectKey(objectKey);
    closingPageDao.update(closingPage);

    log.info("Добавлен файл к завершающей странице surveyId={} fileName={} size={}",
        surveyId, file.getOriginalFilename(), file.getSize());

    return FileResponseDto.builder()
        .fileName(file.getOriginalFilename())
        .fileSize(file.getSize())
        .build();
  }

  @Transactional
  public FileResponseDto updateFile(UUID surveyId, UUID accountId, MultipartFile file) {
    validateFile(file);

    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    permissionService.checkCanEdit(closingPage.getSurvey().getId(), accountId);

    if (closingPage.getFileObjectKey() != null) {
      objectStorageService.deleteObject(closingPage.getFileObjectKey());
    }

    byte[] fileBytes = getFileBytes(file);

    String clearFileName = getClearFileName(file);

    String objectKey = "closing-pages/%s/%s".formatted(surveyId, clearFileName);
    objectStorageService.putObject(objectKey, fileBytes, file.getContentType());

    closingPage.setFileObjectKey(objectKey);
    closingPageDao.update(closingPage);

    log.info("Обновлен файл завершающей страницы surveyId={} fileName={} size={}",
        surveyId, file.getOriginalFilename(), file.getSize());

    return FileResponseDto.builder()
        .fileName(file.getOriginalFilename())
        .fileSize(file.getSize())
        .build();
  }

  @Transactional
  public void deleteFile(UUID surveyId, UUID accountId) {
    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    permissionService.checkCanEdit(closingPage.getSurvey().getId(), accountId);

    if (closingPage.getFileObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Завершающая страница не содержит файла");
    }

    objectStorageService.deleteObject(closingPage.getFileObjectKey());

    closingPage.setFileObjectKey(null);
    closingPageDao.update(closingPage);
    log.info("Удален файл завершающей страницы surveyId={}", surveyId);
  }

  @Transactional(readOnly = true)
  public ObjectUrlResponseDto getFileUrl(UUID surveyId, UUID accountId) {
    ClosingPage closingPage = closingPageDao.findBySurveyId(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Завершающая страница не найдена для опроса: " + surveyId));

    if (closingPage.getFileObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Файл не найден");
    }

    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(
            closingPage.getFileObjectKey(),
            attachmentUrlMaxAge
        ).toString()
    );
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Файл не может быть пустым");
    }

    if (file.getSize() > maxFileSize) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          String.format("Размер файла не должен превышать %d MB", maxFileSize / 1024 / 1024));
    }

    String contentType = file.getContentType();
    if (contentType != null) {
      String lower = contentType.toLowerCase();
      if (lower.contains("executable")
          || lower.contains("x-msdownload")
          || lower.contains("x-javascript")
          || lower.contains("x-sh")) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Исполняемые файлы запрещены");
      }
    }
  }

  private byte[] getFileBytes(MultipartFile file) {
    try {
      return file.getBytes();
    } catch (IOException e) {
      log.error("Ошибка при чтении файла: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Ошибка при чтении файла: " + e.getMessage()
      );
    }
  }

  private String getClearFileName(MultipartFile file) {
    return file.getOriginalFilename()
        .replaceAll("[^a-zA-Zа-яА-Я0-9\\s._-]", "")
        .trim()
        .replace(" ", "_");
  }
}
