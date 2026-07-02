package ru.hh.kakdela_v2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.hh.kakdela_v2.model.NotificationSchedule;

import java.time.*;

@Service
@Slf4j
public class ScheduleCalculationService {

  public Instant calculateNextExecution(NotificationSchedule schedule) {
    Instant now = Instant.now();
    ZoneId userZone = ZoneId.of(schedule.getUserTimezone());

    // Конвертация в ZonedDateTime для работы с датой/временем
    ZonedDateTime nowInUserZone = now.atZone(userZone);
    LocalTime execTime = schedule.getExecutionTime();

    ZonedDateTime candidate = nowInUserZone.with(execTime);

    // Если время уже прошло - переходим на следующий день
    if (candidate.isBefore(nowInUserZone) || candidate.equals(nowInUserZone)) {
      candidate = candidate.plusDays(1);
    }

    candidate = applyScheduleRules(schedule, candidate, nowInUserZone);

    return candidate.toInstant();
  }

  private ZonedDateTime applyScheduleRules(
      NotificationSchedule schedule,
      ZonedDateTime candidate,
      ZonedDateTime now) {

    return schedule.getScheduleType().findNext(schedule, candidate, now);
  }
}
