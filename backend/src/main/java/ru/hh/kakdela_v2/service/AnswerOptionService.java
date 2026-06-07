package ru.hh.kakdela_v2.service;

import ru.hh.kakdela_v2.dao.AnswerOptionDao;
import ru.hh.kakdela_v2.dao.QuestionDao;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionCreateDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.model.AnswerOption;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.util.TransactionHelper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

public class AnswerOptionService {

  private final AnswerOptionDao answerOptionDao;
  private final PermissionService permissionService;
  private final QuestionDao questionDao;
  private final TransactionHelper transactionHelper;

  public AnswerOptionService(AnswerOptionDao answerOptionDao,PermissionService permissionService, QuestionDao questionDao, TransactionHelper transactionHelper) {
    this.answerOptionDao = answerOptionDao;
    this.permissionService = permissionService;
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

  public AnswerOptionResponseDto create(UUID questionId, AnswerOptionCreateDto dto, UUID userId) {
    return transactionHelper.inTransaction(() -> {
      Question question = questionDao.findById(questionId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));

      permissionService.checkAccess(question.getSurveyPage().getSurvey().getId(), userId, SurveyRole.EDITOR);        

      AnswerOption option = AnswerOption.builder()
              .question(question)
              .serialNumber(dto.getSerialNumber())
              .answerOptionText(dto.getAnswerOptionText())
              .build();

      answerOptionDao.save(option);
      return new AnswerOptionResponseDto(option);
    });
  }

  public void delete(UUID id, UUID userId) {
    transactionHelper.inTransaction(() -> {
      AnswerOption option = answerOptionDao.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + id));
            
            
      permissionService.checkOwnership(option.getQuestion().getSurveyPage().getSurvey().getId(), userId);
      answerOptionDao.delete(id);
    });
  }
}
