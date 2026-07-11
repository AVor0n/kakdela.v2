package ru.hh.kakdela.v2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.model.NotificationSchedule;
import ru.hh.kakdela.v2.model.NotificationSchedule.ScheduleType;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit тесты ScheduleCalculationService")
class ScheduleCalculationServiceTest {

  private ScheduleCalculationService service;
  private NotificationSchedule schedule;

  @BeforeEach
  void setUp() {
    schedule = new NotificationSchedule();
    schedule.setTargetTimezone("UTC");
  }

  private ScheduleCalculationService withFixedClock(String instant) {
    return new ScheduleCalculationService(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
  }

  @Test
  @DisplayName("DAILY: должен вернуть сегодня, когда время выполнения в будущем")
  void calculateNextExecution_DailyTimeInFuture_shouldReturnToday() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));

    service = withFixedClock("2026-07-08T09:00:00Z");

    assertEquals(Instant.parse("2026-07-08T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("DAILY: должен вернуть завтра, когда время выполнения прошло")
  void calculateNextExecution_DailyTimePassed_shouldReturnTomorrow() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));

    service = withFixedClock("2026-07-08T11:00:00Z");

    assertEquals(Instant.parse("2026-07-09T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("DAILY: должен вернуть завтра, когда текущее время равно времени выполнения")
  void calculateNextExecution_DailyTimeEqualsNow_shouldReturnTomorrow() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));

    service = withFixedClock("2026-07-08T10:00:00Z");

    assertEquals(Instant.parse("2026-07-09T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("DAILY: должен корректно обрабатывать выполнение в полночь")
  void calculateNextExecution_DailyMidnight_shouldReturnNextDay() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(0, 0));

    service = withFixedClock("2026-07-08T23:00:00Z");

    assertEquals(Instant.parse("2026-07-09T00:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("DAILY: должен корректно обрабатывать выполнение в 23:59")
  void calculateNextExecution_DailyEndOfDay_shouldReturnToday() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(23, 59));

    service = withFixedClock("2026-07-08T10:00:00Z");

    assertEquals(Instant.parse("2026-07-08T23:59:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("WEEKLY: должен вернуть тот же день, когда время в будущем")
  void calculateNextExecution_WeeklyTimeInFuture_shouldReturnToday() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(14, 30));
    schedule.setDaysOfWeek(4);

    service = withFixedClock("2026-07-08T10:00:00Z");

    assertEquals(Instant.parse("2026-07-08T14:30:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("WEEKLY: должен вернуть следующий выбранный день, когда время прошло")
  void calculateNextExecution_WeeklyTimePassed_shouldReturnNextDay() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDaysOfWeek(21);

    service = withFixedClock("2026-07-08T15:00:00Z");

    assertEquals(Instant.parse("2026-07-10T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("WEEKLY: должен перейти на следующую неделю, когда дней на неделе не осталось")
  void calculateNextExecution_WeeklyNoDaysLeft_shouldMoveToNextWeek() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDaysOfWeek(7);

    service = withFixedClock("2026-07-08T15:00:00Z");

    assertEquals(Instant.parse("2026-07-13T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("WEEKLY: должен корректно обрабатывать расписание только на выходных")
  void calculateNextExecution_WeeklyWeekendOnly_shouldReturnNextWeekend() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(9, 0));
    schedule.setDaysOfWeek(96);

    service = withFixedClock("2026-07-08T10:00:00Z");

    assertEquals(Instant.parse("2026-07-11T09:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("WEEKLY: должен корректно обрабатывать расписание на один день")
  void calculateNextExecution_WeeklySingleDay_shouldReturnCorrectDay() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(9, 0));
    schedule.setDaysOfWeek(32);

    service = withFixedClock("2026-07-08T10:00:00Z");

    assertEquals(Instant.parse("2026-07-11T09:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("WEEKLY: должен корректно обрабатывать выполнение в воскресенье")
  void calculateNextExecution_WeeklySunday_shouldReturnNextSunday() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDaysOfWeek(64);

    service = withFixedClock("2026-07-08T10:00:00Z");

    assertEquals(Instant.parse("2026-07-12T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("MONTHLY: должен вернуть тот же месяц, когда день в будущем")
  void calculateNextExecution_MonthlyDayInFuture_shouldReturnSameMonth() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(12, 0));
    schedule.setDayOfMonth(15);

    service = withFixedClock("2026-07-08T10:00:00Z");

    assertEquals(Instant.parse("2026-07-15T12:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("MONTHLY: должен вернуть следующий месяц, когда день прошёл")
  void calculateNextExecution_MonthlyDayPassed_shouldReturnNextMonth() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(5);

    service = withFixedClock("2026-07-08T15:00:00Z");

    assertEquals(Instant.parse("2026-08-05T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("MONTHLY: должен вернуть следующий месяц, когда тот же день, но время прошло")
  void calculateNextExecution_MonthlySameDayTimePassed_shouldReturnNextMonth() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(8);

    service = withFixedClock("2026-07-08T15:00:00Z");

    assertEquals(Instant.parse("2026-08-08T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("MONTHLY: должен корректно обрабатывать 31-й день, когда в месяце 31 день")
  void calculateNextExecution_MonthlyDay31_shouldReturnSameDay() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(31);

    service = withFixedClock("2026-07-08T10:00:00Z");

    assertEquals(Instant.parse("2026-07-31T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("MONTHLY: должен выбрасывать исключение для 31-го дня, когда в месяце 30 дней")
  void calculateNextExecution_MonthlyDay31MonthHas30Days_shouldThrowException() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(31);

    service = withFixedClock("2026-04-15T10:00:00Z");

    assertThrows(ResponseStatusException.class, () -> service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("MONTHLY: должен корректно обрабатывать 29 февраля в високосный год")
  void calculateNextExecution_MonthlyFeb29LeapYear_shouldReturnFeb29() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(29);

    service = withFixedClock("2024-02-15T10:00:00Z");

    assertEquals(Instant.parse("2024-02-29T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("MONTHLY: для 29 февраля в невисокосный год - выбрасывает исключение в феврале")
  void calculateNextExecution_MonthlyFeb29NonLeapYear_shouldThrowException() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(29);

    service = withFixedClock("2025-02-15T10:00:00Z");

    assertThrows(ResponseStatusException.class, () -> service.calculateNextExecution(schedule));
  }

  @ParameterizedTest
  @CsvSource({
      "Europe/Moscow, 2026-07-08T06:00:00Z, 2026-07-08T07:00:00Z",
      "America/New_York, 2026-07-08T13:00:00Z, 2026-07-08T14:00:00Z",
      "Asia/Tokyo, 2026-07-07T23:00:00Z, 2026-07-08T01:00:00Z",
      "UTC, 2026-07-08T09:00:00Z, 2026-07-08T10:00:00Z"
  })
  @DisplayName("TIMEZONE: должен корректно обрабатывать разные часовые пояса")
  void calculateNextExecution_TimezoneDifferentTimezones_shouldWorkCorrectly(String timezone, String now, String expected) {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setTargetTimezone(timezone);

    service = withFixedClock(now);

    assertEquals(Instant.parse(expected), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("EDGE CASE: должен корректно обрабатывать невалидный часовой пояс")
  void calculateNextExecution_EdgeInvalidTimezone_shouldThrowException() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setTargetTimezone("Invalid/Timezone");

    service = withFixedClock("2026-07-08T09:00:00Z");

    assertThrows(DateTimeException.class, () -> service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("EDGE CASE: должен корректно обрабатывать переход через год")
  void calculateNextExecution_EdgeYearBoundary_shouldHandleCorrectly() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));

    service = withFixedClock("2026-12-31T11:00:00Z");

    assertEquals(Instant.parse("2027-01-01T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("EDGE CASE: должен корректно обрабатывать переход через месяц")
  void calculateNextExecution_EdgeMonthBoundary_shouldHandleCorrectly() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));

    service = withFixedClock("2026-07-31T11:00:00Z");

    assertEquals(Instant.parse("2026-08-01T10:00:00Z"), service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("CUSTOM: должен корректно выполнять cron-выражение")
  void shouldExecuteCustomCronCorrectly() {
    schedule.setScheduleType(ScheduleType.CUSTOM);
    schedule.setCronExpression("0 0 15 * * ?");
    schedule.setTargetTimezone("UTC");

    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    CronTrigger trigger = new CronTrigger(schedule.getCronExpression());
    SimpleTriggerContext context = new SimpleTriggerContext();
    Instant expectedFromTrigger = trigger.nextExecution(context);

    assertEquals(expectedFromTrigger, result);
  }

  @Test
  @DisplayName("CUSTOM: должен выбрасывать исключение при невалидном cron-выражении")
  void calculateNextExecution_CustomInvalidCron_shouldThrowException() {
    schedule.setScheduleType(ScheduleType.CUSTOM);
    schedule.setCronExpression("invalid cron");
    schedule.setTargetTimezone("UTC");

    service = withFixedClock("2026-07-08T10:00:00Z");

    assertThrows(ResponseStatusException.class, () -> service.calculateNextExecution(schedule));
  }
}
