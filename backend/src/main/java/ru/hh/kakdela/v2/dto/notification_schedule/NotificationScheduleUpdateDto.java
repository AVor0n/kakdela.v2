package ru.hh.kakdela.v2.dto.notification_schedule;

import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.model.NotificationSchedule;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
public class NotificationScheduleUpdateDto {
  @NullOrNotBlank(message = "Имя не может быть пустым")
  @Size(max = 255, message = "Имя не может быть длиннее 255 символов")
  private String name;

  private NotificationSchedule.ScheduleType type;
  private Integer daysOfWeek;
  private Integer dayOfMonth;
  @NullOrNotBlank
  private String cronExpression;

  private LocalTime executionTime;
  @NullOrNotBlank
  private String targetTimezone = "Europe/Moscow";
  private Boolean isActive = true;
}
