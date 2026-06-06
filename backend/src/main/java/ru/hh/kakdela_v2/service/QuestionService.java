package ru.hh.kakdela_v2.service;

import ru.hh.kakdela_v2.dao.QuestionDao;
import ru.hh.kakdela_v2.dao.SurveyPageDao;
import ru.hh.kakdela_v2.dto.question.QuestionCreateDto;
import ru.hh.kakdela_v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela_v2.dto.question.QuestionUpdateDto;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.model.SurveyPage;
import ru.hh.kakdela_v2.util.TransactionHelper;

import java.util.List;
import java.util.UUID;

public class QuestionService {

  private final QuestionDao questionDao;
  private final SurveyPageDao surveyPageDao;
  private final TransactionHelper transactionHelper;

  public QuestionService(QuestionDao questionDao, SurveyPageDao surveyPageDao, TransactionHelper transactionHelper) {
    this.questionDao = questionDao;
    this.surveyPageDao = surveyPageDao;
    this.transactionHelper = transactionHelper;
  }

  public QuestionResponseDto getById(UUID id) {
    return transactionHelper.inTransaction(() -> {
      Question question = questionDao.findById(id)
              .orElseThrow(() -> new RuntimeException("Вопрос не найден: " + id));
      return new QuestionResponseDto(question);
    });
  }

  public List<QuestionResponseDto> getAllByPageId(UUID pageId) {
    return transactionHelper.inTransaction(() ->
            questionDao.findAllByPageId(pageId).stream()
                    .map(QuestionResponseDto::new)
                    .toList()
    );
  }

  public QuestionResponseDto create(UUID pageId, QuestionCreateDto dto) {
    return transactionHelper.inTransaction(() -> {
      SurveyPage page = surveyPageDao.findById(pageId)
              .orElseThrow(() -> new RuntimeException("Страница не найдена: " + pageId));

      Question question = Question.builder()
              .surveyPage(page)
              .serialNumber(dto.getSerialNumber())
              .title(dto.getTitle())
              .description(dto.getDescription())
              .type(Question.QuestionType.valueOf(dto.getType()))
              .answerOptionOrder(dto.getAnswerOptionOrder())
              .isMandatory(dto.isMandatory())
              .isVisible(dto.isVisible())
              .condition(dto.getCondition())
              .build();

      questionDao.save(question);
      return new QuestionResponseDto(question);
    });
  }

  public QuestionResponseDto update(UUID id, QuestionUpdateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Question question = questionDao.findById(id)
              .orElseThrow(() -> new RuntimeException("Вопрос не найден: " + id));

      if (dto.getSerialNumber() != null) question.setSerialNumber(dto.getSerialNumber());
      if (dto.getTitle() != null) question.setTitle(dto.getTitle());
      if (dto.getDescription() != null) question.setDescription(dto.getDescription());
      if (dto.getType() != null) question.setType(Question.QuestionType.valueOf(dto.getType()));
      if (dto.getAnswerOptionOrder() != null) question.setAnswerOptionOrder(dto.getAnswerOptionOrder());
      if (dto.getMandatory() != null) question.setMandatory(dto.getMandatory());
      if (dto.getVisible() != null) question.setVisible(dto.getVisible());
      if (dto.getCondition() != null) question.setCondition(dto.getCondition());

      questionDao.update(question);
      return new QuestionResponseDto(question);
    });
  }

  public void delete(UUID id) {
    transactionHelper.inTransaction(() -> {
      questionDao.delete(id);
    });
  }
}
