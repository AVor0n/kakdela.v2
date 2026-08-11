package ru.hh.kakdela.v2.mapper;

import ru.hh.kakdela.v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela.v2.dto.answer.option.selected.SelectedAnswerOptionResponseDto;
import ru.hh.kakdela.v2.model.Answer;
import ru.hh.kakdela.v2.model.SelectedAnswerOption;

public class AnswerMapper {

  public static AnswerResponseDto answerToDto(Answer answer) {
    return new AnswerResponseDto(
        answer.getResponse().getId(),
        answer.getQuestion().getId(),
        answer.getQuestionTextSnapshot(),
        answer.getTextValue(),
        answer.getBooleanValue(),
        answer.getDateValue(),
        answer.getTimeValue(),
        answer.getSelectedAnswerOptions().stream()
            .map((AnswerMapper::selectedAnswerOptionToDto))
            .toList(),
        answer.getAnswerAsString()
    );
  }

  public static SelectedAnswerOptionResponseDto selectedAnswerOptionToDto(
      SelectedAnswerOption selectedAnswerOption) {

    return new SelectedAnswerOptionResponseDto(
        selectedAnswerOption.getId(),
        selectedAnswerOption.getAnswerOptionTextSnapshot()
    );
  }
}
