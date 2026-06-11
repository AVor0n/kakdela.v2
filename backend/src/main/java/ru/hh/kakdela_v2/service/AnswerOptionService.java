package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AnswerOptionDao;
import ru.hh.kakdela_v2.dao.QuestionDao;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionCreateDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionUpdateDto;
import ru.hh.kakdela_v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela_v2.dto.question.QuestionUpdateDto;
import ru.hh.kakdela_v2.model.AnswerOption;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Question;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerOptionService {

  private final AnswerOptionDao answerOptionDao;
  private final PermissionService permissionService;
  private final QuestionDao questionDao;

  @Transactional(readOnly = true)
  public List<AnswerOptionResponseDto> getAllByQuestionId(UUID questionId) {
    return answerOptionDao.findAllByQuestionId(questionId).stream()
            .map(AnswerOptionResponseDto::new)
            .toList();
  }

  @Transactional
  public AnswerOptionResponseDto create(UUID questionId, AnswerOptionCreateDto dto,UUID accountId ) {
    Question question = questionDao.findById(questionId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Вопрос не найден: " + questionId));

    permissionService.checkAccess(question.getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);   

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
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден: " + id));

      permissionService.checkAccess(option.getQuestion().getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);

      if (dto.getSerialNumber() != null) option.setSerialNumber(dto.getSerialNumber());
      if (dto.getAnswerOptionText() != null) option.setAnswerOptionText(dto.getAnswerOptionText());
      answerOptionDao.update(option);
      return new AnswerOptionResponseDto(option);
  }

  @Transactional
  public void delete(UUID answerOptionId,UUID accountId) {
    AnswerOption option = answerOptionDao.findById(answerOptionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вариант ответа не найден: " + answerOptionId));
            
            
    permissionService.checkAccess(option.getQuestion().getSurveyPage().getSurvey().getId(), accountId, SurveyRole.EDITOR);
    answerOptionDao.delete(answerOptionId);
  }
}
