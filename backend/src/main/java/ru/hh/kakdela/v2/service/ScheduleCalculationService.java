package ru.hh.kakdela.v2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.hh.kakdela.v2.model.NotificationSchedule;

import java.time.*;

@Service
@Slf4j
public class ScheduleCalculationService {

  public Instant calculateNextExecution(NotificationSchedule schedule) {
    Instant now = Instant.now();
    ZoneId targetZone = ZoneId.of(schedule.getTargetTimezone());

    // Конвертация в ZonedDateTime для работы с датой/временем
    ZonedDateTime nowInTargetZone = now.atZone(targetZone);
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
