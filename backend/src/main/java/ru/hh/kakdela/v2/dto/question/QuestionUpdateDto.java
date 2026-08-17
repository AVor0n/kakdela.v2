package ru.hh.kakdela.v2.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Question.Update")
public class QuestionUpdateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Min(value = 1, message = ConstraintMessages.SERIAL_NUMBER_SHOULD_BE_POSITIVE)
  private Integer serialNumber;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String text;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String description;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Question.QuestionType type;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Question.AnswerOptionOrder answerOptionOrder;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Boolean hasOtherOption;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Boolean isMandatory;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Boolean isVisible;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String condition;
}
