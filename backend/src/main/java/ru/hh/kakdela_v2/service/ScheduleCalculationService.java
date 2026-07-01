package ru.hh.kakdela_v2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.model.NotificationSchedule;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

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

    return switch (schedule.getScheduleType()) {
      case DAILY -> findNextDaily(candidate, now);
      case WEEKLY -> findNextWeekly(schedule, candidate, now);
      case MONTHLY -> findNextMonthly(schedule, candidate, now);
      case CUSTOM -> findNextCron(schedule, candidate);
    };
  }

  private ZonedDateTime findNextDaily(ZonedDateTime candidate, ZonedDateTime now) {
    return candidate.isAfter(now) ? candidate : candidate.plusDays(1);
  }

  private ZonedDateTime findNextWeekly(NotificationSchedule schedule,
                                       ZonedDateTime candidate,
                                       ZonedDateTime now) {
    int daysOfWeek = schedule.getDaysOfWeek();

    // Проверяем до 7 дней вперед
    for (int i = 0; i < 7; i++) {
      ZonedDateTime checkDate = candidate.plusDays(i);
      int dayOfWeekValue = 1 << (checkDate.getDayOfWeek().getValue() - 1);

      if ((daysOfWeek & dayOfWeekValue) != 0) {
        ZonedDateTime checkDateTime = checkDate.with(candidate.toLocalTime());
        if (checkDateTime.isAfter(now) || checkDateTime.equals(now)) {
          return checkDateTime;
        }
      }
    }

    return candidate.plusWeeks(1);
  }

  private ZonedDateTime findNextMonthly(NotificationSchedule schedule,
                                        ZonedDateTime candidate,
                                        ZonedDateTime now) {
    int dayOfMonth = schedule.getDayOfMonth();

    try {
      ZonedDateTime candidateWithDay = candidate.withDayOfMonth(dayOfMonth);
      if (candidateWithDay.isAfter(now) || candidateWithDay.equals(now)) {
        return candidateWithDay;
      }
    } catch (DateTimeException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невалидное значение времени");
    }

    // Следующий месяц
    ZonedDateTime nextMonth = candidate.plusMonths(1).withDayOfMonth(1);
    try {
      int lastDay = nextMonth.getMonth().length(nextMonth.toLocalDate().isLeapYear());
      int targetDay = Math.min(dayOfMonth, lastDay);
      return nextMonth.withDayOfMonth(targetDay);
    } catch (DateTimeException e) {
      return nextMonth.with(TemporalAdjusters.lastDayOfMonth());
    }
  }

  private ZonedDateTime findNextCron(NotificationSchedule schedule,
                                     ZonedDateTime candidate) {
    try {
      CronTrigger trigger = new CronTrigger(schedule.getCronExpression());
      TriggerContext context = new SimpleTriggerContext();

      Instant nextInstant = trigger.nextExecution(context);
      if (nextInstant == null) {
        log.warn("CRON выражение '{}' не имеет следующего выполнения",
            schedule.getCronExpression());
        return candidate.plusDays(1);
      }
      return nextInstant.atZone(ZoneId.of(schedule.getUserTimezone()));
    } catch (Exception e) {
      log.error("Ошибка парсинга CRON: {}", schedule.getCronExpression(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невалидное CRON выражение");
    }
  }
}
