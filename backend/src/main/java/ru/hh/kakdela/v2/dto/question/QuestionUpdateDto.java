package ru.hh.kakdela.v2.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "Question.Update"
)
public class QuestionUpdateDto {

  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;
  @NullOrNotBlank(message = "Заголовок не должен быть пустым")
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String text;
  private String description;
  private Question.QuestionType type;
  private Question.AnswerOptionOrder answerOptionOrder;
  private Boolean hasOtherOption;
  private Boolean isMandatory;
}
