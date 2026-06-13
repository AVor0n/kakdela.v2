package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AnswerDao;
import ru.hh.kakdela_v2.dao.QuestionDao;
import ru.hh.kakdela_v2.dao.ResponseDao;
import ru.hh.kakdela_v2.dto.answer.AnswerCreateDto;
import ru.hh.kakdela_v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela_v2.model.Answer;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.model.Response;
import ru.hh.kakdela_v2.util.JwtUtil;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerService {

  private final AnswerDao answerDao;
  private final ResponseDao responseDao;
  private final QuestionDao questionDao;
  private final JwtUtil jwtUtil;

  private Response checkAccessAndGetResponse(UUID responseId, UUID accountId, String token) {
    Response response = responseDao.findById(responseId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Прохождение не найдено: " + responseId));

    if (response.getAccount() != null
            && !response.getAccount().getId().equals(accountId)
            || !Objects.equals(jwtUtil.extractSubject(token), responseId.toString())) {
      throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "Вы не являетесь автором ответа");
    }

    return response;
  }

  @Transactional(readOnly = true)
  public List<AnswerResponseDto> getAllByResponseId(UUID responseId, UUID accountId, String token) {
    checkAccessAndGetResponse(responseId, accountId, token);

    return answerDao.findAllByResponseId(responseId).stream()
            .map(AnswerResponseDto::new)
            .toList();
  }

  @Transactional
  public AnswerResponseDto create(UUID responseId, AnswerCreateDto dto, UUID accountId, String token) {
    Response response = checkAccessAndGetResponse(responseId, accountId, token);

    if (response.isComplete()) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT, "Прохождение уже завершено");
    }

    Question question = questionDao.findById(dto.getQuestionId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Вопрос не найден: " + dto.getQuestionId()));

    if (!question.getSurveyPage().getSurvey().getId()
            .equals(response.getSurvey().getId())) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Вопрос не принадлежит опросу этого прохождения");
    }

    Answer.AnswerId answerId = Answer.AnswerId.builder()
            .responseId(responseId)
            .questionId(dto.getQuestionId())
            .build();

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
  }

  @Transactional
  public AnswerResponseDto update(UUID responseId, UUID questionId, String newAnswerText,
                                  UUID accountId, String token) {
    Response response = checkAccessAndGetResponse(responseId, accountId, token);

    if (response.isComplete()) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT, "Нельзя изменить ответ — прохождение уже завершено");
    }

    Answer.AnswerId id = Answer.AnswerId.builder()
            .responseId(responseId)
            .questionId(questionId)
            .build();

    Answer answer = answerDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ответ не найден"));

    answer.setAnswerText(newAnswerText);
    answerDao.update(answer);
    return new AnswerResponseDto(answer);
  }

  @Transactional
  public void delete(UUID responseId, UUID questionId, UUID accountId, String token) {
    checkAccessAndGetResponse(responseId, accountId, token);

    Answer.AnswerId id = Answer.AnswerId.builder()
            .responseId(responseId)
            .questionId(questionId)
            .build();

    Answer answer = answerDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ответ не найден: " + id));

    answerDao.delete(answer);
  }
}
