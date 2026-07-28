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
    name = "Question.Create"
)
public class QuestionCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Заголовок не должен быть пустым")
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String text;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Size(max = 5000, message = "Описание не должно быть длиннее 5000 символов")
  private String description;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Тип вопроса обязателен")
  private Question.QuestionType type;

  // NOT_REQUIRED и NotNull — необязательный, так как есть значение по умолчанию,
  // но не может явно быть null, так как null перетрёт значение по умолчанию

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = "Порядок вариантов ответа не может быть null")
  private Question.AnswerOptionOrder answerOptionOrder =
      Question.AnswerOptionOrder.ORIGINAL;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = "Опция \"Другое\" не может быть null")
  private Boolean hasOtherOption = false;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = "Опция \"Обязательный вопрос\" не может быть null")
  private Boolean isMandatory = true;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = "Опция \"Видимость\" не может быть null")
  private Boolean isVisible = true;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String condition;
}
