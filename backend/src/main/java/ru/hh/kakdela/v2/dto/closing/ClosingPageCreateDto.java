package ru.hh.kakdela.v2.dto.closing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.Messages;
import ru.hh.kakdela.v2.constants.TextValueLengthLimits;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "ClosingPage.Create")
public class ClosingPageCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String title;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String description;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Size(max = TextValueLengthLimits.URL_MAX_LENGTH,
      message = Messages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED_1
          + TextValueLengthLimits.URL_MAX_LENGTH
          + Messages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED_2_1)
  @NullOrNotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String websiteUrl;
}
