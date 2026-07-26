package ru.hh.kakdela.v2.mapper;

import ru.hh.kakdela.v2.dto.answer.option.selected.SelectedAnswerOptionResponseDto;
import ru.hh.kakdela.v2.model.SelectedAnswerOption;

public class SelectedAnswerOptionMapper {

  public static SelectedAnswerOptionResponseDto selectedAnswerOptionToDto(
      SelectedAnswerOption selectedAnswerOption) {

    return new SelectedAnswerOptionResponseDto(
        selectedAnswerOption.getId(),
        selectedAnswerOption.getAnswerOptionTextSnapshot()
    );
  }
}
