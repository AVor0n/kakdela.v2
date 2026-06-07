package ru.hh.kakdela_v2.dto.answer;

import lombok.Getter;
import ru.hh.kakdela_v2.model.Answer;

import java.util.UUID;

@Getter
public class AnswerResponseDto {

  private final UUID responseId;
  private final UUID questionId;
  private final String answerText;

  public AnswerResponseDto(Answer answer) {
    this.responseId = answer.getId().getResponseId();
    this.questionId = answer.getId().getQuestionId();
    this.answerText = answer.getAnswerText();
  }
}
