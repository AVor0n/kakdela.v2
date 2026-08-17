package ru.hh.kakdela.v2.dto.notification.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;
import ru.hh.kakdela.v2.constants.DefaultValues;
import ru.hh.kakdela.v2.constants.TextValueLengthLimits;
import ru.hh.kakdela.v2.model.NotificationSchedule;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Notification.Schedule.Create")
public class NotificationScheduleCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  @Size(max = TextValueLengthLimits.DEFAULT_MAX_LENGTH,
      message = ConstraintMessages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED
          + TextValueLengthLimits.DEFAULT_MAX_LENGTH)
  private String name;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private NotificationSchedule.ScheduleType type;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Integer daysOfWeek;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Min(value = 1, message = "Номер дня месяца не может быть меньше 1")
  @Max(value = 31, message = "Номер дня месяца не может быть больше 31")
  private Integer dayOfMonth;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String cronExpression;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonFormat(pattern = "H:mm")
  private LocalTime executionTime;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String targetTimezone = DefaultValues.TARGET_TIMEZONE_DEFAULT;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private Boolean isActive = DefaultValues.NOTIFICATION_SCHEDULE_IS_ACTIVE;
}
