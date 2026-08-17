package ru.hh.kakdela.v2.dto.notification.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.model.NotificationSchedule;

@AllArgsConstructor
@Getter
@Schema(name = "Notification.Schedule.Response")
public class NotificationScheduleResponseDto {

  private final UUID id;
  private final UUID surveyId;
  private final NotificationSchedule.ScheduleType scheduleType;
  private final Integer daysOfWeek;
  private final Integer dayOfMonth;
  private final String cronExpression;
  private final LocalTime executionTime;
  private final String targetTimezone;
  private final Boolean isActive;
}
