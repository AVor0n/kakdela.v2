package ru.hh.kakdela_v2.dto.question;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionUpdateDto {

  private Integer serialNumber;
  private String title;
  private String description;
  private String type;
  private String answerOptionOrder;
  private Boolean mandatory;
  private Boolean visible;
  private String condition;
}
