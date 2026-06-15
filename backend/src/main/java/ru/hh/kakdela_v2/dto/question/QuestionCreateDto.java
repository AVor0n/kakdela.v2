package ru.hh.kakdela_v2.dto.question;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela_v2.model.Question;

@NoArgsConstructor
@Getter
@Setter
public class QuestionCreateDto {

  @NotNull(message = "Порядковый номер обязателен")
  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;
  @NotBlank(message = "Заголовок не должен быть пустым")
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String title;
  @Size(max = 5000, message = "Описание не должно быть длиннее 5000 символов")
  private String description;
  @NotNull(message = "Тип вопроса обязателен")
  private Question.QuestionType type;
  private Question.AnswerOptionOrder answerOptionOrder;
  @NotNull
  private Boolean isMandatory = true;
  @NotNull
  private Boolean isVisible = true;
  private String condition;
}
