package ru.hh.kakdela.v2.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Question.Create")
public class QuestionCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Min(value = 1, message = ConstraintMessages.SERIAL_NUMBER_SHOULD_BE_POSITIVE)
  private Integer serialNumber;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String text;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String description;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private Question.QuestionType type;

  // NOT_REQUIRED и NotNull — необязательный, так как есть значение по умолчанию,
  // но не может явно быть null, так как null перетрёт значение по умолчанию

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private Question.AnswerOptionOrder answerOptionOrder =
      Question.AnswerOptionOrder.ORIGINAL;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private Boolean hasOtherOption = false;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private Boolean isMandatory = true;
}
