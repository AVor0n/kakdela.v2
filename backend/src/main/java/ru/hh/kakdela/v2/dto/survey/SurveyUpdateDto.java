package ru.hh.kakdela.v2.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;
import ru.hh.kakdela.v2.constants.ConstraintMessages;
import ru.hh.kakdela.v2.validator.JsonNullableUndefinedOrNotNullAndNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Survey.Update")
public class SurveyUpdateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonNullableUndefinedOrNotNullAndNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private JsonNullable<String> title = JsonNullable.undefined();

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private JsonNullable<String> description = JsonNullable.undefined();

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private JsonNullable<Boolean> isAuthorizedOnly = JsonNullable.undefined();

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private JsonNullable<Boolean> isLimitedToOneResponse = JsonNullable.undefined();

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private JsonNullable<Boolean> isPublished = JsonNullable.undefined();

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private JsonNullable<Boolean> doNotify = JsonNullable.undefined();

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private JsonNullable<LocalDateTime> expireAtAtTargetTimezone = JsonNullable.undefined();

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonNullableUndefinedOrNotNullAndNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private JsonNullable<String> targetTimezone = JsonNullable.undefined();
}
