package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AnswerOptionDao;
import ru.hh.kakdela.v2.dao.QuestionDao;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionCreateDto;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionResponseDto;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionUpdateDto;
import ru.hh.kakdela.v2.dto.image.ProcessedImage;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.mapper.AnswerOptionMapper;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.Question;

@Slf4j
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

    permissionService.checkCanEdit(question.getSurveyPage().getSurvey().getId(), accountId);

    int maxAvailableSerial = answerOptionDao.findMaxSerialNumber(questionId) + 1;

    if (dto.getSerialNumber() != null
        && !dto.getSerialNumber().equals(maxAvailableSerial)) {
      if (dto.getSerialNumber() > maxAvailableSerial) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Порядковый номер должен быть не больше " + maxAvailableSerial);
      }

      answerOptionDao.increaseSerialNumbers(questionId, dto.getSerialNumber());
    }

    AnswerOption answerOption = AnswerOption.builder()
        .id(UUID.randomUUID())
        .question(question)
        .serialNumber(dto.getSerialNumber() != null
            ? dto.getSerialNumber()
            : maxAvailableSerial)
        .text(dto.getText())
        .build();

    answerOptionDao.save(answerOption);
    log.info("Создан вариант ответа id={} questionId={}", answerOption.getId(), questionId);
    return answerOptionMapper.answerOptionToDto(answerOption);
  }

  @Transactional
  public AnswerOptionResponseDto update(UUID id, AnswerOptionUpdateDto dto, UUID accountId) {
    AnswerOption answerOption = answerOptionDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));

    permissionService.checkCanEdit(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId);

    UUID questionId = answerOption.getQuestion().getId();
    int oldSerial = answerOption.getSerialNumber();

    if (dto.getSerialNumber() != null && !dto.getSerialNumber().equals(oldSerial)) {
      int newSerial = dto.getSerialNumber();
      int maxAvailableSerial = answerOptionDao.findMaxSerialNumber(questionId);
      if (newSerial > maxAvailableSerial) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Новый номер должен быть не больше " + maxAvailableSerial);
      }

      if (oldSerial > newSerial) {
        answerOptionDao.increaseSerialNumbers(questionId, newSerial, oldSerial - 1);
      } else {
        answerOptionDao.decreaseSerialNumbers(questionId, oldSerial + 1, newSerial);
      }

      answerOption.setSerialNumber(newSerial);
    }

    if (dto.getText() != null) {
      answerOption.setText(dto.getText());
    }
    answerOptionDao.update(answerOption);
    log.info("Изменен вариант ответа id={}", id);
    return answerOptionMapper.answerOptionToDto(answerOption);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    AnswerOption answerOption = answerOptionDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Вариант ответа не найден: " + id));

    Question question = answerOption.getQuestion();

    permissionService.checkCanEdit(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId);
    UUID questionId = question.getId();
    int deletedSerial = answerOption.getSerialNumber();

    answerOptionDao.delete(answerOption);

    answerOptionDao.decreaseSerialNumbers(questionId, deletedSerial + 1);
    log.info("Удален вариант ответа id={}", id);
  }

  // Attachment management

  @Transactional
  public ObjectUrlResponseDto addAttachment(UUID answerOptionId, UUID accountId,
                                            MultipartFile file) {
    AnswerOption answerOption = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId));

    permissionService.checkCanEdit(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId);

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
    log.info("Добавлено вложение к варианту ответа id={} objectKey={}", answerOptionId, objectKey);

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

    permissionService.checkCanEdit(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId);

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
    log.info("Изменено вложение варианта ответа id={} objectKey={}", answerOptionId, objectKey);

    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public void deleteAttachment(UUID answerOptionId, UUID accountId) {
    AnswerOption answerOption = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId));

    permissionService.checkCanEdit(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId);

    if (answerOption.getAttachmentObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Вариант ответа не содержит вложения");
    }

    objectStorageService.deleteObject(answerOption.getAttachmentObjectKey());

    answerOption.setAttachmentObjectKey(null);
    answerOptionDao.update(answerOption);
    log.info("Удалено вложение варианта ответа id={}", answerOptionId);
  }

  // Вспомогательные методы

  @Transactional(readOnly = true)
  public List<AnswerOption> getByIdsAndVerifyByQuestionId(
      Set<UUID> answerOptionIds,
      UUID questionId
  ) {
    List<AnswerOption> result = answerOptionDao.findByIds(answerOptionIds);

    if (result.size() != answerOptionIds.size()) {
      Set<UUID> foundIds = result.stream()
          .map(AnswerOption::getId)
          .collect(Collectors.toSet());

      Set<UUID> missingIds = answerOptionIds.stream()
          .filter(id -> !foundIds.contains(id))
          .collect(Collectors.toSet());

      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Варианты ответа не найдены: ids=" + missingIds);
    }

    List<UUID> answerOptionsOfAnotherQuestionIds = result.stream()
        .filter(ao -> !ao.getQuestion().getId().equals(questionId))
        .map(AnswerOption::getId)
        .toList();

    if (!answerOptionsOfAnotherQuestionIds.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Варианты ответа принадлежат другому вопросу: ids="
              + answerOptionsOfAnotherQuestionIds);
    }

    return result;
  }
}
