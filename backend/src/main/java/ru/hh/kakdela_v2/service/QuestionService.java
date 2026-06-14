package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.QuestionDao;
import ru.hh.kakdela_v2.dao.SurveyPageDao;
import ru.hh.kakdela_v2.dto.question.QuestionCreateDto;
import ru.hh.kakdela_v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela_v2.dto.question.QuestionUpdateDto;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.model.SurveyPage;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionService {

  private final QuestionDao questionDao;
  private final PermissionService permissionService;
  private final SurveyPageDao surveyPageDao;

  @Transactional(readOnly = true)
  public QuestionResponseDto getById(UUID id) {
    Question question = questionDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));
    return new QuestionResponseDto(question);
  }

  @Transactional(readOnly = true)
  public List<QuestionResponseDto> getAllByPageId(UUID pageId) {
    return questionDao.findAllByPageId(pageId).stream()
            .map(QuestionResponseDto::new)
            .toList();
  }

  @Transactional
  public QuestionResponseDto create(UUID pageId, QuestionCreateDto dto, UUID accountId) {
    if (questionDao.existsByPageIdAndSerialNumber(pageId, dto.getSerialNumber())) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              "Вопрос с номером " + dto.getSerialNumber() + " уже существует на этой странице");
    }

    SurveyPage page = surveyPageDao.findById(pageId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Страница не найдена: " + pageId));
    
    permissionService.checkAccess(page.getSurvey().getId(), accountId, SurveyRole.EDITOR);

    Question.QuestionBuilder questionBuilder = Question.builder();

    questionBuilder.surveyPage(page)
        .serialNumber(dto.getSerialNumber())
        .title(dto.getTitle())
        .description(dto.getDescription())
        .type(dto.getType())
        .answerOptionOrder(dto.getAnswerOptionOrder())
        .condition(dto.getCondition());

    if (dto.getIsMandatory() != null) {
      questionBuilder.isMandatory(dto.getIsMandatory());
    }
    if (dto.getIsVisible() != null) {
      questionBuilder.isMandatory(dto.getIsVisible());
    }

    Question question = questionBuilder.build();

    questionDao.save(question);
    return new QuestionResponseDto(question);
  }

  @Transactional
  public QuestionResponseDto update(UUID questionId, QuestionUpdateDto dto, UUID accountId) {
    Question question = questionDao.findById(questionId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));
                    
    permissionService.checkAccess(question.getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);

    if (dto.getSerialNumber() != null) question.setSerialNumber(dto.getSerialNumber());
    if (dto.getTitle() != null) question.setTitle(dto.getTitle());
    if (dto.getDescription() != null) question.setDescription(dto.getDescription());
    if (dto.getType() != null) question.setType(dto.getType());
    if (dto.getAnswerOptionOrder() != null) question.setAnswerOptionOrder(dto.getAnswerOptionOrder());
    if (dto.getMandatory() != null) question.setMandatory(dto.getMandatory());
    if (dto.getVisible() != null) question.setVisible(dto.getVisible());
    if (dto.getCondition() != null) question.setCondition(dto.getCondition());

    questionDao.update(question);
    return new QuestionResponseDto(question);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    Question question = questionDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));
    permissionService.checkAccess(question.getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);
    questionDao.delete(question);
  }
}
