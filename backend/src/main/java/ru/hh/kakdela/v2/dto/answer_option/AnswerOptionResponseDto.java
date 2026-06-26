package ru.hh.kakdela.v2.dto.answer_option;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class AnswerOptionResponseDto {

  private final UUID id;
  private final int serialNumber;
  private final String answerOptionText;
  private final String attachmentUrl;
}
