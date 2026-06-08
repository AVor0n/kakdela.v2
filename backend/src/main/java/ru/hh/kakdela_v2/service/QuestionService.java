package ru.hh.kakdela_v2.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import ru.hh.kakdela_v2.dao.QuestionDao;
import ru.hh.kakdela_v2.dao.SurveyPageDao;
import ru.hh.kakdela_v2.dto.question.QuestionCreateDto;
import ru.hh.kakdela_v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela_v2.dto.question.QuestionUpdateDto;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.model.SurveyPage;
import ru.hh.kakdela_v2.util.TransactionHelper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class QuestionService {

  private final QuestionDao questionDao;
  private final PermissionService permissionService;
  private final SurveyPageDao surveyPageDao;
  private final TransactionHelper transactionHelper;

  public QuestionResponseDto getById(UUID id) {
    return transactionHelper.inTransaction(() -> {
      Question question = questionDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));
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

  public QuestionResponseDto create(UUID pageId, QuestionCreateDto dto, UUID accountId) {
    return transactionHelper.inTransaction(() -> {
      SurveyPage page = surveyPageDao.findById(pageId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена: " + pageId));
      
      permissionService.checkAccess(page.getSurvey().getId(), accountId, SurveyRole.EDITOR);

      if (questionDao.existsByPageIdAndSerialNumber(pageId, dto.getSerialNumber())) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Вопрос с номером " + dto.getSerialNumber() + " уже существует на этой странице"
        );
      }

      Question question = Question.builder()
              .surveyPage(page)
              .serialNumber(dto.getSerialNumber())
              .title(dto.getTitle())
              .description(dto.getDescription())
              .type(parseQuestionType(dto.getType()))
              .answerOptionOrder(dto.getAnswerOptionOrder())
              .isMandatory(dto.isMandatory())
              .isVisible(dto.isVisible())
              .condition(dto.getCondition())
              .build();
      questionDao.save(question);
      return new QuestionResponseDto(question);
    });
  }

  public QuestionResponseDto update(UUID id, QuestionUpdateDto dto, UUID accountId) {
    return transactionHelper.inTransaction(() -> {
      Question question = questionDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));

      permissionService.checkAccess(question.getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);

      if (dto.getSerialNumber() != null) question.setSerialNumber(dto.getSerialNumber());
      if (dto.getTitle() != null) question.setTitle(dto.getTitle());
      if (dto.getDescription() != null) question.setDescription(dto.getDescription());
      if (dto.getType() != null) question.setType(parseQuestionType(dto.getType()));
      if (dto.getAnswerOptionOrder() != null) question.setAnswerOptionOrder(dto.getAnswerOptionOrder());
      if (dto.getMandatory() != null) question.setMandatory(dto.getMandatory());
      if (dto.getVisible() != null) question.setVisible(dto.getVisible());
      if (dto.getCondition() != null) question.setCondition(dto.getCondition());

      questionDao.update(question);
      return new QuestionResponseDto(question);
    });
  }

  public void delete(UUID id, UUID accountId) {
    transactionHelper.inTransaction(() -> {
      Question question = questionDao.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));
            
            permissionService.checkAccess(question.getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);
      questionDao.delete(id);
    });
  }

  private Question.QuestionType parseQuestionType(String type) {
    try {
      return Question.QuestionType.valueOf(type);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Недопустимый тип вопроса: " + type + ". Допустимые значения: "
                      + Arrays.toString(Question.QuestionType.values())
      );
    }
  }
}
