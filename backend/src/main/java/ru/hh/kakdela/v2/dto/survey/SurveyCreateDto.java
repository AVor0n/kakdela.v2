package ru.hh.kakdela.v2.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;
import ru.hh.kakdela.v2.constants.DefaultValues;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Survey.Create")
public class SurveyCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String title;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String description;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private Boolean isAuthorizedOnly = DefaultValues.IS_AUTHORIZED_ONLY_DEFAULT;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private Boolean isLimitedToOneResponse = DefaultValues.IS_LIMITED_TO_ONE_RESPONSE_DEFAULT;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private Boolean doNotify = DefaultValues.DO_NOTIFY_DEFAULT;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private LocalDateTime expireAtAtTargetTimezone;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String targetTimezone = DefaultValues.TARGET_TIMEZONE_DEFAULT;
}
