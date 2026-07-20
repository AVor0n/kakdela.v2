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
import ru.hh.kakdela.v2.dao.SurveyPageDao;
import ru.hh.kakdela.v2.dto.image.ProcessedImage;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.dto.question.QuestionCreateDto;
import ru.hh.kakdela.v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela.v2.dto.question.QuestionUpdateDto;
import ru.hh.kakdela.v2.mapper.QuestionMapper;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.Permission.SurveyRole;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.SurveyPage;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final QuestionDao questionDao;
  private final PermissionService permissionService;
  private final SurveyPageDao surveyPageDao;
  private final ObjectStorageService objectStorageService;
  private final QuestionMapper questionMapper;
  private final ImageProcessingService imageProcessingService;

  @Transactional(readOnly = true)
  public QuestionResponseDto getById(UUID id) {
    Question question = questionDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));
    return questionMapper.questionToDto(question);
  }

  @Transactional(readOnly = true)
  public List<QuestionResponseDto> getAllByPageId(UUID pageId) {
    return questionDao.findAllByPageId(pageId).stream()
        .map(questionMapper::questionToDto)
        .toList();
  }

  @Transactional
  public QuestionResponseDto create(UUID pageId, QuestionCreateDto dto, UUID accountId) {
    SurveyPage page = surveyPageDao.findById(pageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: " + pageId));

    permissionService.checkAccess(page.getSurvey().getId(), accountId, SurveyRole.EDITOR);

    int maxAvailableSerial = questionDao.findMaxSerialNumber(pageId) + 1;

    if (dto.getSerialNumber() != null
        && !dto.getSerialNumber().equals(maxAvailableSerial)) {
      if (dto.getSerialNumber() > maxAvailableSerial) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Порядковый номер должен быть не больше " + maxAvailableSerial);
      }

      questionDao.increaseSerialNumbers(pageId, dto.getSerialNumber());
    }

    Question question = Question.builder()
        .surveyPage(page)
        .serialNumber(dto.getSerialNumber() != null
            ? dto.getSerialNumber()
            : maxAvailableSerial)
        .title(dto.getTitle())
        .description(dto.getDescription())
        .type(dto.getType())
        .answerOptionOrder(dto.getAnswerOptionOrder())
        .isMandatory(dto.getIsMandatory())
        .isVisible(dto.getIsVisible())
        .condition(dto.getCondition())
        .build();

    questionDao.save(question);
    log.info("Создан вопрос id={} pageId={}", question.getId(), pageId);
    return questionMapper.questionToDto(question);
  }

  @Transactional
  public QuestionResponseDto clone(UUID questionId, UUID accountId) {
    Question originalQuestion = questionDao.findById(questionId)
        .orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Вопрос " + questionId + " не найден"
            )
        );

    permissionService.checkAccess(
        originalQuestion.getSurveyPage().getSurvey().getId(),
        accountId,
        SurveyRole.EDITOR
    );

    Question questionCopy = Question.builder()
        .surveyPage(originalQuestion.getSurveyPage())
        .serialNumber(originalQuestion.getSerialNumber() + 1)
        .title(originalQuestion.getTitle())
        .description(originalQuestion.getDescription())
        .attachmentObjectKey(originalQuestion.getAttachmentObjectKey())
        .type(originalQuestion.getType())
        .answerOptionOrder(originalQuestion.getAnswerOptionOrder())
        .isMandatory(originalQuestion.isMandatory())
        .isVisible(originalQuestion.isVisible())
        .condition(originalQuestion.getCondition())
        .answers(List.of())
        .build();

    for (AnswerOption originalOption : originalQuestion.getAnswerOptions()) {
      AnswerOption optionCopy = AnswerOption.builder()
          .question(questionCopy)
          .serialNumber(originalOption.getSerialNumber())
          .answerOptionText(originalOption.getAnswerOptionText())
          .build();
      questionCopy.getAnswerOptions().add(optionCopy);
    }

    questionDao.increaseSerialNumbers(
        originalQuestion.getSurveyPage().getId(),
        originalQuestion.getSerialNumber() + 1
    );
    questionDao.save(questionCopy);

    return questionMapper.questionToDto(questionCopy);
  }

  @Transactional
  public QuestionResponseDto update(UUID questionId, QuestionUpdateDto dto, UUID accountId) {
    Question question = questionDao.findById(questionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));

    permissionService.checkAccess(question.getSurveyPage().getSurvey().getId(), accountId,
        SurveyRole.EDITOR);

    UUID pageId = question.getSurveyPage().getId();
    int oldSerial = question.getSerialNumber();

    if (dto.getSerialNumber() != null && !dto.getSerialNumber().equals(oldSerial)) {
      int newSerial = dto.getSerialNumber();
      int maxAvailableSerial = questionDao.findMaxSerialNumber(pageId);
      if (newSerial > maxAvailableSerial) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Новый номер должен быть не больше " + maxAvailableSerial);
      }

      if (oldSerial > newSerial) {
        questionDao.increaseSerialNumbers(pageId, newSerial, oldSerial - 1);
      } else {
        questionDao.decreaseSerialNumbers(pageId, oldSerial + 1, newSerial);
      }

      question.setSerialNumber(newSerial);
    }

    if (dto.getTitle() != null) {
      question.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      question.setDescription(dto.getDescription());
    }
    if (dto.getType() != null) {
      question.setType(dto.getType());
    }
    if (dto.getAnswerOptionOrder() != null) {
      question.setAnswerOptionOrder(dto.getAnswerOptionOrder());
    }
    if (dto.getIsMandatory() != null) {
      question.setMandatory(dto.getIsMandatory());
    }
    if (dto.getIsVisible() != null) {
      question.setVisible(dto.getIsVisible());
    }
    if (dto.getCondition() != null) {
      question.setCondition(dto.getCondition());
    }

    questionDao.update(question);
    log.info("Изменен вопрос id={}", questionId);
    return questionMapper.questionToDto(question);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    Question question = questionDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));
    permissionService.checkAccess(question.getSurveyPage().getSurvey().getId(), accountId,
        SurveyRole.EDITOR);

    UUID pageId = question.getSurveyPage().getId();
    int deletedSerial = question.getSerialNumber();

    questionDao.delete(question);

    questionDao.decreaseSerialNumbers(pageId, deletedSerial + 1);
    log.info("Удален вопрос id={}", id);
  }

  // Attachment management

  @Transactional
  public ObjectUrlResponseDto addAttachment(UUID questionId, UUID accountId, MultipartFile file) {
    Question question = questionDao.findById(questionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));

    permissionService.checkAccess(
        question.getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);

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
    Question question = questionDao.findById(questionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));

    permissionService.checkAccess(
        question.getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);

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
    Question question = questionDao.findById(questionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));

    permissionService.checkAccess(
        question.getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);

    if (question.getAttachmentObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Вопрос не содержит вложения");
    }

    objectStorageService.deleteObject(question.getAttachmentObjectKey());

    question.setAttachmentObjectKey(null);
    questionDao.update(question);
    log.info("Удалено вложение вопроса id={}", questionId);
  }
}
