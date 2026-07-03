package ru.hh.kakdela.v2.dto.notification_schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.model.NotificationSchedule;

import java.time.LocalTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class NotificationScheduleResponseDto {
  private final UUID id;
  private final UUID surveyId;
  private final NotificationSchedule.ScheduleType scheduleType;
  private final Integer daysOfWeek;
  private final Integer dayOfMonth;
  private final String cronExpression;
  private final LocalTime executionTime;
  private final String userTimezone;
  private final Boolean isActive;
}
