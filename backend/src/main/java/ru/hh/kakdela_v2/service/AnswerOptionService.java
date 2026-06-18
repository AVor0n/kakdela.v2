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
import ru.hh.kakdela_v2.model.AnswerOption;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Question;

@Service
@RequiredArgsConstructor
public class AnswerOptionService {

  private final AnswerOptionDao answerOptionDao;
  private final PermissionService permissionService;
  private final QuestionDao questionDao;
  private final ObjectStorageService objectStorageService;

  @Transactional(readOnly = true)
  public List<AnswerOptionResponseDto> getAllByQuestionId(UUID questionId) {
    return answerOptionDao.findAllByQuestionId(questionId).stream()
        .map(AnswerOptionResponseDto::new)
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

    AnswerOption option = AnswerOption.builder()
        .question(question)
        .serialNumber(dto.getSerialNumber())
        .answerOptionText(dto.getAnswerOptionText())
        .build();

    answerOptionDao.save(option);
    return new AnswerOptionResponseDto(option);
  }

  @Transactional
  public AnswerOptionResponseDto update(UUID id, AnswerOptionUpdateDto dto, UUID accountId) {
    AnswerOption option = answerOptionDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));

    permissionService.checkAccess(option.getQuestion().getSurveyPage().getSurvey().getId(),
        accountId, SurveyRole.EDITOR);

    if (dto.getSerialNumber() != null) {
      option.setSerialNumber(dto.getSerialNumber());
    }
    if (dto.getAnswerOptionText() != null) {
      option.setAnswerOptionText(dto.getAnswerOptionText());
    }
    answerOptionDao.update(option);
    return new AnswerOptionResponseDto(option);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    AnswerOption option = answerOptionDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Вариант ответа не найден: " + id));
    permissionService.checkAccess(option.getQuestion().getSurveyPage().getSurvey().getId(),
        accountId, SurveyRole.EDITOR);
    answerOptionDao.delete(option);
  }

  // Attachment management

  @Transactional
  public ObjectUrlResponseDto addAttachment(UUID answerOptionId, UUID accountId, MultipartFile file) {
    AnswerOption option = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId)
        );

    permissionService.checkAccess(
        option.getQuestion().getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR
    );

    if (option.getAttachmentObjectKey() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Вариант ответа уже содержит вложение"
      );
    }

    return upsertAttachmentHelper(option, file);
  }

  @Transactional
  public ObjectUrlResponseDto updateAttachment(UUID answerOptionId, UUID accountId, MultipartFile file) {
    AnswerOption option = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId)
        );

    permissionService.checkAccess(
        option.getQuestion().getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR
    );

    if (option.getAttachmentObjectKey() != null) {
      objectStorageService.deleteObject(
          option.getAttachmentObjectKey()
      );
    }

    return upsertAttachmentHelper(option, file);
  }

  @Transactional
  public void deleteAttachment(UUID answerOptionId, UUID accountId) {
    AnswerOption option = answerOptionDao.findById(answerOptionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId)
        );

    permissionService.checkAccess(
        option.getQuestion().getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR
    );

    if (option.getAttachmentObjectKey() == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Вариант ответа не содержит вложения"
      );
    }

    objectStorageService.deleteObject(option.getAttachmentObjectKey());

    option.setAttachmentObjectKey(null);
    answerOptionDao.update(option);
  }

  private ObjectUrlResponseDto upsertAttachmentHelper(AnswerOption option, MultipartFile file) {
    // TODO: добавить проверку, что файл является изображением (или, например, видео)
    // TODO: Добавить сжатие для изображений

    String objectKey = "answer-options/%s/%s".formatted(option.getId(), UUID.randomUUID());
    objectStorageService.putObject(
        objectKey,
        file,
        file.getContentType()
    );

    option.setAttachmentObjectKey(objectKey);
    answerOptionDao.update(option);

    return new ObjectUrlResponseDto(
        objectStorageService.generateObjectUrl(objectKey, Duration.ofMinutes(1))
    );
  }
}
