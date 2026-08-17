package ru.hh.kakdela.v2.dto.notification.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.DefaultValues;
import ru.hh.kakdela.v2.model.NotificationSchedule;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
public class NotificationScheduleCreateDto {
  @NotBlank(message = "Имя не может быть пустым")
  @Size(max = 255, message = "Имя не может быть длиннее 255 символов")
  private String name;

  @NotNull
  private NotificationSchedule.ScheduleType type;
  private Integer daysOfWeek;
  @Min(value = 1, message = "Номер дня месяца не может быть меньше 1")
  @Max(value = 31, message = "Номер дня месяца не может быть больше 31")
  private Integer dayOfMonth;
  @NullOrNotBlank
  private String cronExpression;

  @JsonFormat(pattern = "H:mm")
  private LocalTime executionTime;
  @NullOrNotBlank
  private String targetTimezone = DefaultValues.TARGET_TIMEZONE_DEFAULT;
  private boolean isActive = true;
}
