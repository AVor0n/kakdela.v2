package ru.hh.kakdela_v2.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AnswerDao;
import ru.hh.kakdela_v2.dao.QuestionDao;
import ru.hh.kakdela_v2.dao.ResponseDao;
import ru.hh.kakdela_v2.dto.answer.AnswerCreateDto;
import ru.hh.kakdela_v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela_v2.model.Answer;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.model.Response;
import ru.hh.kakdela_v2.util.TransactionHelper;

import java.util.List;
import java.util.UUID;

public class AnswerService {

  private final AnswerDao answerDao;
  private final ResponseDao responseDao;
  private final QuestionDao questionDao;
  private final TransactionHelper transactionHelper;

  public AnswerService(AnswerDao answerDao, ResponseDao responseDao,
                       QuestionDao questionDao, TransactionHelper transactionHelper) {
    this.answerDao = answerDao;
    this.responseDao = responseDao;
    this.questionDao = questionDao;
    this.transactionHelper = transactionHelper;
  }

  public List<AnswerResponseDto> getAllByResponseId(UUID responseId) {
    return transactionHelper.inTransaction(() ->
            answerDao.findAllByResponseId(responseId).stream()
                    .map(AnswerResponseDto::new)
                    .toList()
    );
  }

  public AnswerResponseDto create(UUID responseId, AnswerCreateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Response response = responseDao.findById(responseId)
              .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND, "Прохождение не найдено: " + responseId));

      // нельзя отвечать на завершенное прохождение
      if (response.isComplete()) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Прохождение уже завершено");
      }

      Question question = questionDao.findById(dto.getQuestionId())
              .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND, "Вопрос не найден: " + dto.getQuestionId()));

      // проверка — вопрос принадлежит тому же опросу
      if (!question.getSurveyPage().getSurvey().getId()
              .equals(response.getSurvey().getId())) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Вопрос не принадлежит опросу этого прохождения");
      }

      Answer.AnswerId answerId = Answer.AnswerId.builder()
              .responseId(responseId)
              .questionId(dto.getQuestionId())
              .build();

      // проверка — ответ на этот вопрос уже существует
      if (answerDao.findById(answerId).isPresent()) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Ответ на этот вопрос уже существует");
      }

      Answer answer = Answer.builder()
              .id(answerId)
              .response(response)
              .question(question)
              .answerText(dto.getAnswerText())
              .build();

      answerDao.save(answer);
      return new AnswerResponseDto(answer);
    });
  }

  public void delete(UUID responseId, UUID questionId) {
    transactionHelper.inTransaction(() -> {
      Answer.AnswerId id = Answer.AnswerId.builder()
              .responseId(responseId)
              .questionId(questionId)
              .build();
      answerDao.delete(id);
    });
  }
}
