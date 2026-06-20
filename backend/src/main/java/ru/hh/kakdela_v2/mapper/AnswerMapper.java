package ru.hh.kakdela_v2.mapper;

import ru.hh.kakdela_v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela_v2.model.Answer;

public class AnswerMapper {

  public static AnswerResponseDto answerToDto(Answer answer) {
    return new AnswerResponseDto(
        answer.getId().getResponseId(),
        answer.getId().getQuestionId(),
        answer.getAnswerText()
    );
  }
}
