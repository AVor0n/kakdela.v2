package ru.hh.kakdela.v2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.hh.kakdela.v2.model.NotificationSchedule;
import ru.hh.kakdela.v2.model.NotificationSchedule.ScheduleType;

import java.time.*;

@Service
@Slf4j
public class ScheduleCalculationService {

  private final Clock clock;

  // Конструктор по умолчанию для Spring
  public ScheduleCalculationService() {
    this.clock = Clock.systemUTC();
  }

  // Конструктор для тестов
  public ScheduleCalculationService(Clock clock) {
    this.clock = clock;
  }


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
