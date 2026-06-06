package ru.hh.kakdela_v2.dto.question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateDto {

  private Integer serialNumber;
  private String title;
  private String description;
  private String type;
  private String answerOptionOrder;
  private boolean isMandatory;
  private boolean isVisible;
  private String condition;
}
