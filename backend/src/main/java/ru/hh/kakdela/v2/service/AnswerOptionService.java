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
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionCreateDto;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionResponseDto;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionUpdateDto;
import ru.hh.kakdela.v2.dto.image.ProcessedImage;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.exception.question.AnswerOptionNotFoundException;
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
  private final QuestionService questionService;
  private final PermissionService permissionService;
  private final ObjectStorageService objectStorageService;
  private final AnswerOptionMapper answerOptionMapper;
  private final ImageProcessingService imageProcessingService;

  @Transactional(readOnly = true)
  public AnswerOptionResponseDto getById(UUID optionId, UUID currentUserId) {
    AnswerOption option = getEntityById(optionId);

    permissionService.checkHasAnyPermission(
        option.getQuestion().getSurveyPage().getSurvey().getId(), currentUserId);

    return answerOptionMapper.answerOptionToDto(option);
  }

  @Transactional(readOnly = true)
  public List<AnswerOptionResponseDto> getAllByQuestionId(UUID questionId, UUID currentUserId) {
    Question question = questionService.getEntityById(questionId);

    permissionService.checkHasAnyPermission(
        question.getSurveyPage().getSurvey().getId(), currentUserId);

    return answerOptionDao.findAllByQuestionId(questionId).stream()
        .map(answerOptionMapper::answerOptionToDto)
        .toList();
  }

  @Transactional
  public AnswerOptionResponseDto create(
      UUID questionId,
      AnswerOptionCreateDto dto,
      UUID accountId
  ) {
    Question question = questionService.getEntityById(questionId);

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

    AnswerOption option = AnswerOption.builder()
        .id(UUID.randomUUID())
        .question(question)
        .serialNumber(dto.getSerialNumber() != null
            ? dto.getSerialNumber()
            : maxAvailableSerial)
        .text(dto.getText())
        .build();

    answerOptionDao.save(option);
    log.info("Создан вариант ответа id={} questionId={}", option.getId(), questionId);
    return answerOptionMapper.answerOptionToDto(option);
  }

  @Transactional
  public AnswerOptionResponseDto update(
      UUID optionId,
      AnswerOptionUpdateDto dto,
      UUID accountId
  ) {
    AnswerOption option = getEntityById(optionId);

    permissionService.checkCanEdit(
        option.getQuestion().getSurveyPage().getSurvey().getId(), accountId);

    UUID questionId = option.getQuestion().getId();
    int oldSerial = option.getSerialNumber();

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

      option.setSerialNumber(newSerial);
    }

    if (dto.getText() != null) {
      option.setText(dto.getText());
    }
    answerOptionDao.update(option);
    log.info("Изменен вариант ответа optionId={}", optionId);
    return answerOptionMapper.answerOptionToDto(option);
  }

  @Transactional
  public void delete(UUID optionId, UUID accountId) {
    AnswerOption option = getEntityById(optionId);

    Question question = option.getQuestion();

    permissionService.checkCanEdit(
        option.getQuestion().getSurveyPage().getSurvey().getId(), accountId);
    UUID questionId = question.getId();
    int deletedSerial = option.getSerialNumber();

    answerOptionDao.delete(option);

    answerOptionDao.decreaseSerialNumbers(questionId, deletedSerial + 1);
    log.info("Удален вариант ответа id={}", optionId);
  }

  // Attachment management

  @Transactional
  public ObjectUrlResponseDto addAttachment(
      UUID optionId,
      UUID accountId,
      MultipartFile file
  ) {
    AnswerOption option = getEntityById(optionId);

    permissionService.checkCanEdit(
        option.getQuestion().getSurveyPage().getSurvey().getId(), accountId);

    if (option.getAttachmentObjectKey() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Вариант ответа уже содержит вложение");
    }

    ProcessedImage image = imageProcessingService.process(file);

    String objectKey = "answer-options/%s/%s".formatted(option.getId(), UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    option.setAttachmentObjectKey(objectKey);
    answerOptionDao.update(option);
    log.info("Добавлено вложение к варианту ответа id={} objectKey={}", optionId, objectKey);

    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public ObjectUrlResponseDto updateAttachment(
      UUID optionId,
      UUID accountId,
      MultipartFile file
  ) {
    AnswerOption option = getEntityById(optionId);

    permissionService.checkCanEdit(
        option.getQuestion().getSurveyPage().getSurvey().getId(), accountId);

    ProcessedImage image = imageProcessingService.process(file);

    if (option.getAttachmentObjectKey() != null) {
      objectStorageService.deleteObject(
          option.getAttachmentObjectKey());
    }

    String objectKey = "answer-options/%s/%s".formatted(option.getId(), UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        image.getContent(),
        image.getContentType());

    option.setAttachmentObjectKey(objectKey);
    answerOptionDao.update(option);
    log.info("Изменено вложение варианта ответа id={} objectKey={}", optionId, objectKey);

    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, attachmentUrlMaxAge).toString());
  }

  @Transactional
  public void deleteAttachment(UUID optionId, UUID accountId) {
    AnswerOption option = getEntityById(optionId);

    permissionService.checkCanEdit(
        option.getQuestion().getSurveyPage().getSurvey().getId(), accountId);

    if (option.getAttachmentObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Вариант ответа не содержит вложения");
    }

    objectStorageService.deleteObject(option.getAttachmentObjectKey());

    option.setAttachmentObjectKey(null);
    answerOptionDao.update(option);
    log.info("Удалено вложение варианта ответа id={}", optionId);
  }

  // Вспомогательные методы

  List<AnswerOption> getByIdsAndVerifyByQuestionId(
      Set<UUID> optionIds,
      UUID questionId
  ) {
    List<AnswerOption> result = answerOptionDao.findByIds(optionIds);

    if (result.size() != optionIds.size()) {
      Set<UUID> foundIds = result.stream()
          .map(AnswerOption::getId)
          .collect(Collectors.toSet());

      Set<UUID> missingIds = optionIds.stream()
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

  AnswerOption getEntityById(UUID id) {
    return answerOptionDao.findById(id)
        .orElseThrow(() -> new AnswerOptionNotFoundException(id));
  }
}
