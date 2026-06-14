package ru.hh.kakdela_v2.dto.question;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.util.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
public class QuestionUpdateDto {

  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;
  @NullOrNotBlank(message = "Заголовок не должен быть пустым")
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String title;
  @Size(max = 5000, message = "Описание не должно быть длиннее 5000 символов")
  private String description;
  private Question.QuestionType type;
  private Question.AnswerOptionOrder answerOptionOrder;
  private Boolean isMandatory;
  private Boolean isVisible;
  private String condition;
}
