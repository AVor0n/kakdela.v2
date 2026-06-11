package ru.hh.kakdela_v2.dto.question;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ru.hh.kakdela_v2.model.Question;

@Getter
@Setter
@NoArgsConstructor
public class QuestionUpdateDto {

  private Integer serialNumber;
  private String title;
  private String description;
  private Question.QuestionType type;
  private Question.AnswerOptionOrder answerOptionOrder;
  private Boolean mandatory;
  private Boolean visible;
  private String condition;
}
