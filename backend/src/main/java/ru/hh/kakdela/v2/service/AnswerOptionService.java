package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AnswerOptionDao;
import ru.hh.kakdela.v2.dao.QuestionDao;
import ru.hh.kakdela.v2.dto.answer_option.AnswerOptionCreateDto;
import ru.hh.kakdela.v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela.v2.dto.answer_option.AnswerOptionUpdateDto;
import ru.hh.kakdela.v2.dto.image.ProcessedImage;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.mapper.AnswerOptionMapper;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.Permission.SurveyRole;
import ru.hh.kakdela.v2.model.Question;

@Service
@RequiredArgsConstructor
public class AnswerOptionService {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final AnswerOptionDao answerOptionDao;
  private final PermissionService permissionService;
  private final QuestionDao questionDao;
  private final ObjectStorageService objectStorageService;
  private final AnswerOptionMapper answerOptionMapper;
  private final ImageProcessingService imageProcessingService;

  @Transactional(readOnly = true)
  public List<AnswerOptionResponseDto> getAllByQuestionId(UUID questionId) {
    return answerOptionDao.findAllByQuestionId(questionId).stream()
        .map(answerOptionMapper::answerOptionToDto)
        .toList();
  }

  @Transactional
  public AnswerOptionResponseDto create(UUID questionId,
                                        AnswerOptionCreateDto dto,
                                        UUID accountId) {
    Question question = questionDao.findById(questionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));

    permissionService.checkAccess(
        question.getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);

        answerOptionDao.shiftSerialNumbersUp(questionId, dto.getSerialNumber(), +1);

    AnswerOption answerOption = AnswerOption.builder()
        .question(question)
        .serialNumber(dto.getSerialNumber())
        .answerOptionText(dto.getAnswerOptionText())
        .build();

    answerOptionDao.save(answerOption);
    return answerOptionMapper.answerOptionToDto(answerOption);
  }

  @Transactional
  public AnswerOptionResponseDto update(UUID id, AnswerOptionUpdateDto dto, UUID accountId) {
    AnswerOption answerOption = answerOptionDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));

    permissionService.checkAccess(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(),
        accountId, SurveyRole.EDITOR);

    if (dto.getSerialNumber() != null) {
      answerOption.setSerialNumber(dto.getSerialNumber());
    }
    if (dto.getAnswerOptionText() != null) {
      answerOption.setAnswerOptionText(dto.getAnswerOptionText());
    }
    answerOptionDao.update(answerOption);
    return answerOptionMapper.answerOptionToDto(answerOption);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    AnswerOption answerOption = answerOptionDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Вариант ответа не найден: " + id));
            
        Question question = answerOption.getQuestion();

        permissionService.checkAccess(answerOption.getQuestion().getSurveyPage().getSurvey().getId(),
        accountId, SurveyRole.EDITOR);
        UUID questionId = question.getId();
        int deletedSerial = answerOption.getSerialNumber();

        answerOptionDao.delete(answerOption);

        answerOptionDao.shiftSerialNumbersDown(questionId, deletedSerial + 1, -1);
  }

  // Attachment management

  @Transactional
  public ObjectUrlResponseDto addAttachment(UUID answerOptionId, UUID accountId,
                                            MultipartFile file) {
    AnswerOption answerOption = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId));

    permissionService.checkAccess(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(),
        accountId, SurveyRole.EDITOR);

    if (answerOption.getAttachmentObjectKey() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Вариант ответа уже содержит вложение");
    }

    ProcessedImage image = imageProcessingService.process(file);

    String objectKey = "answer-options/%s/%s".formatted(answerOption.getId(), UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    answerOption.setAttachmentObjectKey(objectKey);
    answerOptionDao.update(answerOption);

    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public ObjectUrlResponseDto updateAttachment(UUID answerOptionId,
                                               UUID accountId,
                                               MultipartFile file) {
    AnswerOption answerOption = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId));

    permissionService.checkAccess(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId,
        SurveyRole.EDITOR);

    ProcessedImage image = imageProcessingService.process(file);

    if (answerOption.getAttachmentObjectKey() != null) {
      objectStorageService.deleteObject(
          answerOption.getAttachmentObjectKey());
    }

    String objectKey = "answer-options/%s/%s".formatted(answerOption.getId(), UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    answerOption.setAttachmentObjectKey(objectKey);
    answerOptionDao.update(answerOption);

    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public void deleteAttachment(UUID answerOptionId, UUID accountId) {
    AnswerOption answerOption = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId));

    permissionService.checkAccess(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId,
        SurveyRole.EDITOR);

    if (answerOption.getAttachmentObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Вариант ответа не содержит вложения");
    }

    objectStorageService.deleteObject(answerOption.getAttachmentObjectKey());

    answerOption.setAttachmentObjectKey(null);
    answerOptionDao.update(answerOption);
  }
}
