package ru.hh.kakdela_v2.dto.answer_option;

import lombok.Getter;
import ru.hh.kakdela_v2.model.AnswerOption;

import java.util.UUID;

@Getter
public class AnswerOptionResponseDto {

  private final UUID id;
  private final Integer serialNumber;
  private final String answerOptionText;

  public AnswerOptionResponseDto(AnswerOption answerOption) {
    this.id = answerOption.getId();
    this.serialNumber = answerOption.getSerialNumber();
    this.answerOptionText = answerOption.getAnswerOptionText();
  }
}
