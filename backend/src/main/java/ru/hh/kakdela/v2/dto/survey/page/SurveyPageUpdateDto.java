package ru.hh.kakdela.v2.dto.survey.page;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.Messages;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "SurveyPage.Update")
public class SurveyPageUpdateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Min(value = 1, message = Messages.SERIAL_NUMBER_SHOULD_BE_POSITIVE)
  private Integer serialNumber;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String title;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String description;
}
