package ru.hh.kakdela.v2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.kakdela.v2.dao.AnswerDao;
import ru.hh.kakdela.v2.dto.answer.AnswerRequestDto;
import ru.hh.kakdela.v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela.v2.dto.answer.AnswerResponseDtoWithStatusDto;
import ru.hh.kakdela.v2.dto.answer.AnswerWithStatusDto;
import ru.hh.kakdela.v2.exception.BadRequestDataException;
import ru.hh.kakdela.v2.exception.question.QuestionNotFoundException;
import ru.hh.kakdela.v2.exception.response.AnswerNotFoundException;
import ru.hh.kakdela.v2.exception.response.ResponseBranchClosedException;
import ru.hh.kakdela.v2.exception.response.ResponseNotFoundOrCompletedException;
import ru.hh.kakdela.v2.mapper.AnswerMapper;
import ru.hh.kakdela.v2.model.Answer;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.SelectedAnswerOption;
import ru.hh.kakdela.v2.status.ObjectStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {

  private final AnswerDao answerDao;
  private final QuestionService questionService;
  private final ResponseService responseService;
  private final AnswerOptionService answerOptionService;

  @Transactional(readOnly = true)
  public List<AnswerResponseDto> getAllByResponseId(UUID responseId, UUID accountId, String token) {
    Response response =
        responseService.getEntityByIdWithOwnerOrSurveyTeamAccessCheck(
            responseId, accountId, token);

    if (response.getAccount() == null && response.isCompleted()
        && !response.getSurvey().isAuthor(accountId)) {
      throw new ResponseNotFoundOrCompletedException(responseId);
    }

    return response.getAnswers().stream()
        .map(AnswerMapper::answerToDto)
        .toList();
  }

  @Transactional
  public AnswerResponseDtoWithStatusDto upsert(
      UUID responseId,
      UUID questionId,
      AnswerRequestDto dto,
      UUID accountId,
      String token
  ) {
    Response response =
        responseService.getEntityWithPageStatusesByIdWithOwnerAccessCheck(
            responseId, accountId, token);

    if (response.isCompleted()) {
      throw new ResponseNotFoundOrCompletedException(responseId);
    }

    Question question = questionService.getEntityById(questionId);

    if (!questionService.getParentSurveyIdById(questionId)
        .equals(response.getSurvey().getId())) {
      throw new QuestionNotFoundException(questionId);
    }

    if (!responseService.isPageIncluded(response, question.getSurveyPage().getId())) {
      throw new ResponseBranchClosedException();
    }

    verifyAnswerRequestDto(dto, question);

    List<AnswerOption> selectedAnswerOptions = new ArrayList<>();
    if (dto.getSelectedAnswerOptionIds() != null && !dto.getSelectedAnswerOptionIds().isEmpty()) {
      selectedAnswerOptions.addAll(
          answerOptionService.getByIdsAndVerifyByQuestionId(
              dto.getSelectedAnswerOptionIds(), questionId));
    }

    AnswerWithStatusDto answerWithStatusDto =
        answerDao.findByResponseIdAndQuestion(responseId, questionId)
            .map(a -> update(a, response, question, dto, selectedAnswerOptions))
            .orElseGet(() -> create(response, question, dto, selectedAnswerOptions));

    responseService.resetResponsePageStatuses(responseId, question.getSurveyPage().getId());

    return new AnswerResponseDtoWithStatusDto(
        AnswerMapper.answerToDto(answerWithStatusDto.getAnswer()),
        answerWithStatusDto.getStatus());
  }

  @Transactional
  public void delete(UUID responseId, UUID questionId, UUID accountId, String token) {
    Response response =
        responseService.getEntityByIdWithOwnerAccessCheck(responseId, accountId, token);

    if (response.isCompleted()) {
      throw new ResponseNotFoundOrCompletedException(responseId);
    }

    Answer answer = answerDao.findByResponseIdAndQuestion(responseId, questionId)
        .orElseThrow(() -> new AnswerNotFoundException(responseId, questionId));

    answerDao.delete(answer);
    log.info("Удалён ответ на вопрос: responseId={} questionId={}", responseId, questionId);

    responseService.resetResponsePageStatuses(
        responseId, questionService.getParentPageIdById(questionId));
  }

  // Вспомогательные методы

  private AnswerWithStatusDto update(
      Answer answer,
      Response response,
      Question question,
      AnswerRequestDto dto,
      List<AnswerOption> selectedAnswerOptions
  ) {
    responseService.resetResponsePageStatuses(
        response.getId(), question.getSurveyPage().getId());

    answer.setPageSerialNumber(questionService.getParentPageSerialNumberById(question.getId()));
    answer.setQuestionSerialNumber(question.getSerialNumber());
    answer.setQuestionTextSnapshot(question.getTextAsPlainString());
    answer.setTextValue(dto.getTextValue());
    answer.setBooleanValue(dto.getBooleanValue());
    answer.setDateValue(dto.getDateValue());
    answer.setTimeValue(dto.getTimeValue());

    answer.getSelectedAnswerOptions().clear();
    answer.getSelectedAnswerOptions()
        .addAll(selectedAnswerOptions.stream()
            .map(ao -> new SelectedAnswerOption(
                UUID.randomUUID(),
                answer,
                ao,
                ao.getSerialNumber(),
                ao.getAnswerOptionTextAsPlainString()))
            .toList());

    answerDao.update(answer);
    log.info("Заменён ответ на вопрос: responseId={} questionId={}",
        response.getId(), question.getId());

    return new AnswerWithStatusDto(answer, ObjectStatus.UPDATED);
  }

  private AnswerWithStatusDto create(
      Response response,
      Question question,
      AnswerRequestDto dto,
      List<AnswerOption> selectedAnswerOptions
  ) {
    Answer answer = Answer.builder()
        .id(UUID.randomUUID())
        .response(response)
        .question(question)
        .pageSerialNumber(questionService.getParentPageSerialNumberById(question.getId()))
        .questionSerialNumber(question.getSerialNumber())
        .questionTextSnapshot(question.getTextAsPlainString())
        .textValue(dto.getTextValue())
        .booleanValue(dto.getBooleanValue())
        .dateValue(dto.getDateValue())
        .timeValue(dto.getTimeValue())
        .build();

    answer.getSelectedAnswerOptions()
        .addAll(selectedAnswerOptions.stream()
            .map(ao -> new SelectedAnswerOption(
                UUID.randomUUID(),
                answer,
                ao,
                ao.getSerialNumber(),
                ao.getAnswerOptionTextAsPlainString()))
            .toList());

    answerDao.save(answer);
    log.info("Создан ответ на вопрос: responseId={} questionId={}",
        response.getId(), question.getId());

    return new AnswerWithStatusDto(answer, ObjectStatus.CREATED);
  }

  private void verifyAnswerRequestDto(
      AnswerRequestDto dto,
      Question question
  ) {

    final Question.QuestionType questionType = question.getType();
    final boolean isOtherOptionAllowedForThisQuestion =
        questionType.isOtherOptionAllowed && question.hasOtherOption();

    if (questionType.isTextAllowed) {
      if (questionType.isOtherOptionAllowed) {
        if (!question.hasOtherOption() && dto.getTextValue() != null) {
          throw new BadRequestDataException(
              "Для данного вопроса не допускается вариант ответа \"Другое\"");
        }
      } else {
        if (dto.getTextValue() == null) {
          throw new BadRequestDataException(
              "Ответ на вопрос типа %s должен иметь текстовое значение"
                  .formatted(questionType));
        }
      }
    } else {
      if (dto.getTextValue() != null) {
        throw new BadRequestDataException(
            "Ответ на вопрос типа %s не должен иметь текстового значения"
                .formatted(questionType));
      }
    }

    if (questionType.isBooleanAllowed) {
      if (dto.getBooleanValue() == null) {
        throw new BadRequestDataException(
            "Ответ на вопрос типа %s должен иметь булевое значение"
                .formatted(questionType));
      }
    } else {
      if (dto.getBooleanValue() != null) {
        throw new BadRequestDataException(
            "Ответ на вопрос типа %s не должен иметь булевого значения"
                .formatted(questionType));
      }
    }

    if (questionType.isDateAllowed) {
      if (dto.getDateValue() == null) {
        throw new BadRequestDataException(
            "Ответ на вопрос типа %s должен иметь значение даты"
                .formatted(questionType));
      }
    } else {
      if (dto.getDateValue() != null) {
        throw new BadRequestDataException(
            "Ответ на вопрос типа %s не должен иметь значения даты"
                .formatted(questionType));
      }
    }

    if (questionType.isTimeAllowed) {
      if (dto.getTimeValue() == null) {
        throw new BadRequestDataException(
            "Ответ на вопрос типа %s должен иметь значение времени"
                .formatted(questionType));
      }
    } else {
      if (dto.getTimeValue() != null) {
        throw new BadRequestDataException(
            "Ответ на вопрос типа %s не должен иметь значения времени"
                .formatted(questionType));
      }
    }

    if (questionType.isAnswerOptionsAllowed) {
      if (dto.getSelectedAnswerOptionIds() == null
          || dto.getSelectedAnswerOptionIds().isEmpty()) {
        if (isOtherOptionAllowedForThisQuestion) {
          if (dto.getTextValue() == null) {
            if (questionType.isMultipleChoiceAllowed) {
              throw new BadRequestDataException(
                  ("Ответ на вопрос типа %s должен ссылаться на варианты ответа "
                      + "или иметь текстовое значение для варианта ответа \"Другое\"")
                      .formatted(questionType));
            } else {
              throw new BadRequestDataException(
                  ("Ответ на вопрос типа %s должен ссылаться ровно на один вариант ответа "
                      + "или иметь текстовое значение для варианта ответа \"Другое\"")
                      .formatted(questionType));
            }
          }
        } else {
          if (questionType.isMultipleChoiceAllowed) {
            throw new BadRequestDataException(
                "Ответ на вопрос типа %s должен ссылаться на варианты ответа"
                    .formatted(questionType));
          } else {
            throw new BadRequestDataException(
                ("Ответ на вопрос типа %s должен ссылаться ровно на один вариант ответа")
                    .formatted(questionType));
          }
        }
      }
      if (dto.getSelectedAnswerOptionIds() != null
          && dto.getSelectedAnswerOptionIds().size() > 1
          && !questionType.isMultipleChoiceAllowed) {
        throw new BadRequestDataException(
            ("Ответ на вопрос типа %s должен ссылаться ровно на один вариант ответа")
                .formatted(questionType));
      }
    } else {
      if (dto.getSelectedAnswerOptionIds() != null) {
        throw new BadRequestDataException(
            "Ответ на вопрос типа %s не должен ссылаться на варианты ответа"
                .formatted(questionType));
      }
    }
  }
}
