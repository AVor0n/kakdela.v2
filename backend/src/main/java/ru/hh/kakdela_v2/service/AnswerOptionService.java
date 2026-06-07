package ru.hh.kakdela_v2.service;

import ru.hh.kakdela_v2.dao.AnswerOptionDao;
import ru.hh.kakdela_v2.dao.QuestionDao;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionCreateDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.model.AnswerOption;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.util.TransactionHelper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

public class AnswerOptionService {

  private final AnswerOptionDao answerOptionDao;
  private final QuestionDao questionDao;
  private final TransactionHelper transactionHelper;

  public AnswerOptionService(AnswerOptionDao answerOptionDao, QuestionDao questionDao, TransactionHelper transactionHelper) {
    this.answerOptionDao = answerOptionDao;
    this.questionDao = questionDao;
    this.transactionHelper = transactionHelper;
  }

  public List<AnswerOptionResponseDto> getAllByQuestionId(UUID questionId) {
    return transactionHelper.inTransaction(() ->
            answerOptionDao.findAllByQuestionId(questionId).stream()
                    .map(AnswerOptionResponseDto::new)
                    .toList()
    );
  }

  public AnswerOptionResponseDto create(UUID questionId, AnswerOptionCreateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Question question = questionDao.findById(questionId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));

      AnswerOption option = AnswerOption.builder()
              .question(question)
              .serialNumber(dto.getSerialNumber())
              .answerOptionText(dto.getAnswerOptionText())
              .build();

      answerOptionDao.save(option);
      return new AnswerOptionResponseDto(option);
    });
  }

  public void delete(UUID id) {
    transactionHelper.inTransaction(() -> {
      answerOptionDao.delete(id);
    });
  }
}
