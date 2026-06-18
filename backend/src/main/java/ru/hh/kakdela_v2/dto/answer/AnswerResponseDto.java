package ru.hh.kakdela_v2.dto.answer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.model.Answer;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class AnswerResponseDto {

  private final UUID responseId;
  private final UUID questionId;
  private final String answerText;
}
