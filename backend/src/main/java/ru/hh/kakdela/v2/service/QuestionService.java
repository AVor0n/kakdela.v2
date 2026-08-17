package ru.hh.kakdela.v2.service;

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
import ru.hh.kakdela.v2.dao.QuestionDao;
import ru.hh.kakdela.v2.dto.image.ProcessedImage;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.dto.question.QuestionCreateDto;
import ru.hh.kakdela.v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela.v2.dto.question.QuestionUpdateDto;
import ru.hh.kakdela.v2.exception.question.QuestionNotFoundException;
import ru.hh.kakdela.v2.mapper.QuestionMapper;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.util.DataConstraintUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final QuestionDao questionDao;
  private final SurveyService surveyService;
  private final SurveyPageService surveyPageService;
  private final PermissionService permissionService;
  private final ObjectStorageService objectStorageService;
  private final ImageProcessingService imageProcessingService;
  private final QuestionMapper questionMapper;

  @Transactional(readOnly = true)
  public QuestionResponseDto getById(UUID questionId, UUID accountId) {
    Question question = getEntityById(questionId);

    permissionService.checkHasAnyPermission(
        questionDao.findParentSurveyIdById(questionId), accountId);

    return questionMapper.questionToDto(question);
  }

  @Transactional(readOnly = true)
  public List<QuestionResponseDto> getAllByPageId(UUID pageId, UUID accountId) {
    SurveyPage page = surveyPageService.getEntityById(pageId);

    permissionService.checkHasAnyPermission(
        page.getSurvey().getId(), accountId);

    return questionDao.findAllByPageId(pageId).stream()
        .map(questionMapper::questionToDto)
        .toList();
  }

  @Transactional
  public QuestionResponseDto create(UUID pageId, QuestionCreateDto dto, UUID accountId) {
    SurveyPage page = surveyPageService.getEntityById(pageId);

    permissionService.checkCanEdit(page.getSurvey().getId(), accountId);

    int maxAvailableSerial = questionDao.findMaxSerialNumber(pageId) + 1;

    if (dto.getSerialNumber() != null
        && !dto.getSerialNumber().equals(maxAvailableSerial)) {
      DataConstraintUtil.checkSerialNumberUpperLimit(dto.getSerialNumber(), maxAvailableSerial);

      questionDao.increaseSerialNumbers(pageId, dto.getSerialNumber());
    }

    DataConstraintUtil.checkQuestionTextLength(dto.getText());
    DataConstraintUtil.checkDescriptionLength(dto.getDescription());

    Question question = Question.builder()
        .id(UUID.randomUUID())
        .surveyPage(page)
        .serialNumber(dto.getSerialNumber() != null
            ? dto.getSerialNumber()
            : maxAvailableSerial)
        .text(dto.getText())
        .description(dto.getDescription())
        .type(dto.getType())
        .answerOptionOrder(dto.getAnswerOptionOrder())
        .hasOtherOption(dto.getHasOtherOption())
        .isMandatory(dto.getIsMandatory())
        .build();

    questionDao.save(question);
    log.info("Создан вопрос id={} pageId={}", question.getId(), pageId);
    return questionMapper.questionToDto(question);
  }

  @Transactional
  public QuestionResponseDto clone(UUID questionId, UUID accountId) {
    Question originalQuestion = getEntityWithParentPageById(questionId);

    permissionService.checkCanEdit(originalQuestion.getSurveyPage().getSurvey().getId(), accountId);

    Question questionCopy = surveyService.cloneQuestion(
        originalQuestion,
        originalQuestion.getSurveyPage(),
        true);

    questionDao.increaseSerialNumbers(
        originalQuestion.getSurveyPage().getId(),
        originalQuestion.getSerialNumber() + 1
    );
    questionDao.save(questionCopy);

    return questionMapper.questionToDto(questionCopy);
  }

  @Transactional
  public QuestionResponseDto update(UUID questionId, QuestionUpdateDto dto, UUID accountId) {
    Question question = getEntityById(questionId);

    permissionService.checkCanEdit(questionDao.findParentSurveyIdById(questionId), accountId);

    UUID pageId = question.getSurveyPage().getId();
    int oldSerial = question.getSerialNumber();

    if (dto.getSerialNumber() != null && !dto.getSerialNumber().equals(oldSerial)) {
      int newSerial = dto.getSerialNumber();
      int maxAvailableSerial = questionDao.findMaxSerialNumber(pageId);

      DataConstraintUtil.checkSerialNumberUpperLimit(newSerial, maxAvailableSerial);

      if (oldSerial > newSerial) {
        questionDao.increaseSerialNumbers(pageId, newSerial, oldSerial - 1);
      } else {
        questionDao.decreaseSerialNumbers(pageId, oldSerial + 1, newSerial);
      }

      question.setSerialNumber(newSerial);
    }

    if (dto.getText() != null) {
      DataConstraintUtil.checkQuestionTextLength(dto.getText());
      question.setText(dto.getText());
    }
    if (dto.getDescription() != null) {
      DataConstraintUtil.checkDescriptionLength(dto.getDescription());
      question.setDescription(dto.getDescription());
    }
    if (dto.getType() != null) {
      question.setType(dto.getType());
    }
    if (dto.getAnswerOptionOrder() != null) {
      question.setAnswerOptionOrder(dto.getAnswerOptionOrder());
    }
    if (dto.getHasOtherOption() != null) {
      question.setHasOtherOption(dto.getHasOtherOption());
    }
    if (dto.getIsMandatory() != null) {
      question.setMandatory(dto.getIsMandatory());
    }

    questionDao.update(question);
    log.info("Изменен вопрос id={}", questionId);
    return questionMapper.questionToDto(question);
  }

  @Transactional
  public void delete(UUID questionId, UUID accountId) {
    Question question = getEntityById(questionId);

    permissionService.checkCanEdit(questionDao.findParentSurveyIdById(questionId), accountId);

    UUID pageId = question.getSurveyPage().getId();
    int deletedSerial = question.getSerialNumber();

    questionDao.delete(question);

    questionDao.decreaseSerialNumbers(pageId, deletedSerial + 1);
    log.info("Удален вопрос id={}", questionId);
  }

  // Attachment management

  @Transactional
  public ObjectUrlResponseDto addAttachment(UUID questionId, UUID accountId, MultipartFile file) {
    Question question = getEntityById(questionId);

    permissionService.checkCanEdit(questionDao.findParentSurveyIdById(questionId), accountId);

    if (question.getAttachmentObjectKey() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "К вопросу уже прикреплено вложение");
    }

    ProcessedImage image = imageProcessingService.process(file);

    String objectKey = "questions/%s/%s".formatted(question.getId(), UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    question.setAttachmentObjectKey(objectKey);
    questionDao.update(question);
    log.info("Добавлено вложение к вопросу id={} objectKey={}", questionId, objectKey);
    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public ObjectUrlResponseDto updateAttachment(UUID questionId,
                                               UUID accountId,
                                               MultipartFile file) {
    Question question = getEntityById(questionId);

    permissionService.checkCanEdit(questionDao.findParentSurveyIdById(questionId), accountId);

    ProcessedImage image = imageProcessingService.process(file);

    if (question.getAttachmentObjectKey() != null) {
      objectStorageService.deleteObject(
          question.getAttachmentObjectKey());
    }

    String objectKey = "questions/%s/%s".formatted(question.getId(), UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    question.setAttachmentObjectKey(objectKey);
    questionDao.update(question);
    log.info("Изменено вложение вопроса id={} objectKey={}", questionId, objectKey);
    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public void deleteAttachment(UUID questionId, UUID accountId) {
    Question question = getEntityById(questionId);

    permissionService.checkCanEdit(questionDao.findParentSurveyIdById(questionId), accountId);

    if (question.getAttachmentObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Вопрос не содержит вложения");
    }

    objectStorageService.deleteObject(question.getAttachmentObjectKey());

    question.setAttachmentObjectKey(null);
    questionDao.update(question);
    log.info("Удалено вложение вопроса id={}", questionId);
  }

  // Вспомогательные методы

  Question getEntityById(UUID id) {
    return questionDao.findById(id)
        .orElseThrow(() -> new QuestionNotFoundException(id));
  }

  Question getEntityWithParentPageById(UUID id) {
    return questionDao.findWithParentPageById(id)
        .orElseThrow(() -> new QuestionNotFoundException(id));
  }

  UUID getParentSurveyIdById(UUID id) {
    return questionDao.findParentSurveyIdById(id);
  }

  UUID getParentPageIdById(UUID id) {
    return questionDao.findParentPageIdById(id);
  }

  int getParentPageSerialNumberById(UUID id) {
    return questionDao.findParentPageSerialNumberById(id);
  }
}
