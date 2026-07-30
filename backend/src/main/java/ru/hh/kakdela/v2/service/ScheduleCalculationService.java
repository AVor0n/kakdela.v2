package ru.hh.kakdela.v2.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.hh.kakdela.v2.model.NotificationSchedule;
import ru.hh.kakdela.v2.model.NotificationSchedule.ScheduleType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleCalculationService {

  private final Clock clock;

  public Instant calculateNextExecution(NotificationSchedule schedule) {
    Instant now = Instant.now(clock);
    ZoneId targetZone = ZoneId.of(schedule.getTargetTimezone());

    // Конвертация в ZonedDateTime для работы с датой/временем
    ZonedDateTime nowInTargetZone = now.atZone(targetZone);

    if (schedule.getScheduleType() == ScheduleType.CUSTOM) {
      ZonedDateTime result = schedule.getScheduleType()
          .findNext(schedule, nowInTargetZone, nowInTargetZone);
      return result.toInstant();
    }

    LocalTime execTime = schedule.getExecutionTime();
    ZonedDateTime candidate = nowInTargetZone.with(execTime);

    // Если время уже прошло - переходим на следующий день
    if (candidate.isBefore(nowInTargetZone) || candidate.equals(nowInTargetZone)) {
      candidate = candidate.plusDays(1);
    }

    candidate = schedule.getScheduleType().findNext(schedule, candidate, nowInTargetZone);

    return candidate.toInstant();
  }

}
