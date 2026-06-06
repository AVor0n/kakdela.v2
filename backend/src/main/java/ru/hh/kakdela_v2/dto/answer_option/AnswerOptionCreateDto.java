package ru.hh.kakdela_v2.dto.answer_option;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOptionCreateDto {

  private Integer serialNumber;
  private String answerOptionText;
}
