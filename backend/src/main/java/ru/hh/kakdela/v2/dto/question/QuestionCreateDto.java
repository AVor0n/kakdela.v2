package ru.hh.kakdela.v2.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.model.Question;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "QuestionCreate",
    title = "DTO для создания вопроса"
)
public class QuestionCreateDto {

  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Заголовок не должен быть пустым")
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String text;
  @Size(max = 5000, message = "Описание не должно быть длиннее 5000 символов")
  private String description;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Тип вопроса обязателен")
  private Question.QuestionType type;
  private Question.AnswerOptionOrder answerOptionOrder;
  @Schema(description = "Это необязательное поле, у него есть дефолтное значение")
  @NotNull
  private Boolean isMandatory = true;
  @Schema(description = "Это необязательное поле, у него есть дефолтное значение")
  @NotNull
  private Boolean isVisible = true;
  private String condition;
}
