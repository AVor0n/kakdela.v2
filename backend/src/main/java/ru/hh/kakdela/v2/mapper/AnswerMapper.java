package ru.hh.kakdela.v2.mapper;

import ru.hh.kakdela.v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela.v2.model.Answer;

public class AnswerMapper {

  public static AnswerResponseDto answerToDto(Answer answer) {
    return new AnswerResponseDto(
        answer.getResponse().getId(),
        answer.getQuestion().getId(),
        answer.getTextValue()
    );
  }
}
