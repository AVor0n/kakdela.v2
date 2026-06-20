package ru.hh.kakdela_v2.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AnswerOptionDao;
import ru.hh.kakdela_v2.dao.QuestionDao;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionCreateDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionUpdateDto;
import ru.hh.kakdela_v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela_v2.mapper.AnswerOptionMapper;
import ru.hh.kakdela_v2.model.AnswerOption;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.validator.ImageValidator;

@Service
@RequiredArgsConstructor
public class AnswerOptionService {

  private final AnswerOptionDao answerOptionDao;
  private final PermissionService permissionService;
  private final QuestionDao questionDao;
  private final ObjectStorageService objectStorageService;
  private final AnswerOptionMapper answerOptionMapper;
  private final ImageValidator imageValidator;

  @Transactional(readOnly = true)
  public List<AnswerOptionResponseDto> getAllByQuestionId(UUID questionId) {
    return answerOptionDao.findAllByQuestionId(questionId).stream()
        .map(answerOptionMapper::answerOptionToDto)
        .toList();
  }

  @Transactional
  public AnswerOptionResponseDto create(UUID questionId, AnswerOptionCreateDto dto,
                                        UUID accountId) {
    Question question = questionDao.findById(questionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));

    permissionService.checkAccess(question.getSurveyPage().getSurvey().getId(), accountId,
        SurveyRole.EDITOR);

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

    permissionService.checkAccess(answerOption.getQuestion().getSurveyPage().getSurvey().getId(),
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
    permissionService.checkAccess(answerOption.getQuestion().getSurveyPage().getSurvey().getId(),
        accountId, SurveyRole.EDITOR);
    answerOptionDao.delete(answerOption);
  }

  // Attachment management

  @Transactional
  public ObjectUrlResponseDto addAttachment(UUID answerOptionId, UUID accountId,
                                            MultipartFile file) {
    AnswerOption answerOption = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId)
        );

    permissionService.checkAccess(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR
    );

    if (answerOption.getAttachmentObjectKey() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Вариант ответа уже содержит вложение"
      );
    }

    return upsertAttachmentHelper(answerOption, file);
  }

  @Transactional
  public ObjectUrlResponseDto updateAttachment(UUID answerOptionId, UUID accountId,
                                               MultipartFile file) {
    AnswerOption answerOption = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId)
        );

    permissionService.checkAccess(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR
    );

    if (answerOption.getAttachmentObjectKey() != null) {
      objectStorageService.deleteObject(
          answerOption.getAttachmentObjectKey()
      );
    }

    return upsertAttachmentHelper(answerOption, file);
  }

  @Transactional
  public void deleteAttachment(UUID answerOptionId, UUID accountId) {
    AnswerOption answerOption = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId)
        );

    permissionService.checkAccess(
        answerOption.getQuestion().getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR
    );

    if (answerOption.getAttachmentObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Вариант ответа не содержит вложения"
      );
    }

    objectStorageService.deleteObject(answerOption.getAttachmentObjectKey());

    answerOption.setAttachmentObjectKey(null);
    answerOptionDao.update(answerOption);
  }

  private ObjectUrlResponseDto upsertAttachmentHelper(AnswerOption answerOption,
                                                      MultipartFile file) {
    // TODO: Добавить сжатие для изображений

    if (!imageValidator.isImage(file)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Загруженное изображение имеет неподдерживаемый формат или испорчено");
    }

    String objectKey = "answer-options/%s/%s".formatted(answerOption.getId(), UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        file,
        file.getContentType()
    );

    answerOption.setAttachmentObjectKey(objectKey);
    answerOptionDao.update(answerOption);

    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, Duration.ofMinutes(1)).toString()
    );
  }
}
