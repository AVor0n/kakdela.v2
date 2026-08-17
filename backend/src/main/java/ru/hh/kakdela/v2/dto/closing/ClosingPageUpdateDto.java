package ru.hh.kakdela.v2.dto.closing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;
import ru.hh.kakdela.v2.constants.TextValueLengthLimits;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "ClosingPage.Update")
public class ClosingPageUpdateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String title;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String description;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Size(max = TextValueLengthLimits.URL_MAX_LENGTH,
      message = ConstraintMessages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED
          + TextValueLengthLimits.URL_MAX_LENGTH)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String websiteUrl;
}
