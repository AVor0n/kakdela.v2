package ru.hh.kakdela_v2.dto.notification_schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela_v2.model.NotificationSchedule;
import ru.hh.kakdela_v2.validator.NullOrNotBlank;

import java.time.LocalTime;

@NoArgsConstructor
@Getter
@Setter
public class NotificationScheduleCreateDto {
  @NotBlank(message = "Имя не может быть пустым")
  @Size(max = 255, message = "Имя не может быть длинее 255 символов")
  private String name;

  @NotNull
  private NotificationSchedule.ScheduleType type;
  private Integer daysOfWeek;
  private Integer dayOfMonth;
  @NullOrNotBlank
  private String cronExpression;

  private LocalTime executionTime;
  @NullOrNotBlank
  private String userTimezone = "Europe/Moscow";
  private boolean isActive = true;
}
