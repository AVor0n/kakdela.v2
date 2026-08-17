package ru.hh.kakdela.v2.dto.answer.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "AnswerOption.Create"
)
public class AnswerOptionCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Min(value = 1, message = ConstraintMessages.SERIAL_NUMBER_SHOULD_BE_POSITIVE)
  private Integer serialNumber;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String text;
}
