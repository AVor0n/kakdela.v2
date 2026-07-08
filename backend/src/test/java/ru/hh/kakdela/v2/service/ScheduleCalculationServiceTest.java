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

import static org.assertj.core.api.Assertions.assertThat;
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
  void daily_timeInFuture_shouldReturnToday() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-07-08T09:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-08T10:00:00Z"));
  }

  @Test
  @DisplayName("DAILY: должен вернуть завтра, когда время выполнения прошло")
  void daily_timePassed_shouldReturnTomorrow() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-07-08T11:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-09T10:00:00Z"));
  }

  @Test
  @DisplayName("DAILY: должен вернуть завтра, когда текущее время равно времени выполнения")
  void daily_timeExactlyNow_shouldReturnTomorrow() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-09T10:00:00Z"));
  }

  @Test
  @DisplayName("DAILY: должен корректно обрабатывать выполнение в полночь")
  void daily_midnight_shouldReturnCorrectDay() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(0, 0));
    service = withFixedClock("2026-07-08T23:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-09T00:00:00Z"));
  }

  @Test
  @DisplayName("DAILY: должен корректно обрабатывать выполнение в 23:59")
  void daily_endOfDay_shouldReturnCorrectDay() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(23, 59));
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-08T23:59:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен вернуть тот же день, когда время в будущем")
  void weekly_timeInFuture_shouldReturnSameDay() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(14, 30));
    schedule.setDaysOfWeek(4);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-08T14:30:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен вернуть следующий выбранный день, когда время прошло")
  void weekly_timePassed_shouldReturnNextDay() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDaysOfWeek(21);
    service = withFixedClock("2026-07-08T15:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-10T10:00:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен перейти на следующую неделю, когда дней на неделе не осталось")
  void weekly_noDaysLeft_shouldMoveToNextWeek() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDaysOfWeek(7);
    service = withFixedClock("2026-07-08T15:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-13T10:00:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен корректно обрабатывать расписание только на выходных")
  void weekly_weekendOnly_shouldReturnNextWeekend() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(9, 0));
    schedule.setDaysOfWeek(96);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-11T09:00:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен корректно обрабатывать расписание на один день")
  void weekly_OneDayOnly_shouldReturnCorrectDay() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(9, 0));
    schedule.setDaysOfWeek(32);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-11T09:00:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен корректно обрабатывать выполнение в воскресенье")
  void weekly_sunday_shouldReturnNextSunday() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDaysOfWeek(64);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-12T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен вернуть тот же месяц, когда день в будущем")
  void monthly_dayInFuture_shouldReturnSameMonth() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(12, 0));
    schedule.setDayOfMonth(15);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-15T12:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен вернуть следующий месяц, когда день прошёл")
  void monthly_dayPassed_shouldReturnNextMonth() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(5);
    service = withFixedClock("2026-07-08T15:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-08-05T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен вернуть следующий месяц, когда тот же день, но время прошло")
  void monthly_sameDayTimePassed_shouldReturnNextMonth() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(8);
    service = withFixedClock("2026-07-08T15:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-08-08T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен корректно обрабатывать 31-й день, когда в месяце 31 день")
  void monthly_day31_shouldReturnCorrectMonth() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(31);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-31T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен выбрасывать исключение для 31-го дня, когда в месяце 30 дней")
  void monthly_day31ForMonthWith30Days_shouldThrowException() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(31);
    service = withFixedClock("2026-04-15T10:00:00Z");

    assertThrows(ResponseStatusException.class, () -> service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("MONTHLY: должен корректно обрабатывать 29 февраля в високосный год")
  void monthly_feb29InLeapYear_shouldReturnFeb29() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(29);
    service = withFixedClock("2024-02-15T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2024-02-29T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: для 29 февраля в невисокосный год - выбрасывает исключение в феврале")
  void monthly_feb29InNonLeapYear_shouldThrowException() {
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
      "UTC, 2026-07-08T09:00:00Z, 2026-07-08T10:00:00Z"})

  @DisplayName("TIMEZONE: должен корректно обрабатывать разные часовые пояса")
  void timezone_shouldWorkCorrectly(String timezone, String now, String expected) {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setTargetTimezone(timezone);
    service = withFixedClock(now);

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse(expected));
  }

  @Test
  @DisplayName("EDGE CASE: должен корректно обрабатывать невалидный часовой пояс")
  void invalidTimezone_shouldThrowException() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setTargetTimezone("Invalid/Timezone");
    service = withFixedClock("2026-07-08T09:00:00Z");

    assertThrows(DateTimeException.class, () -> service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("EDGE CASE: должен корректно обрабатывать переход через год")
  void yearBoundary_shouldHandleCorrectly() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-12-31T11:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2027-01-01T10:00:00Z"));
  }

  @Test
  @DisplayName("EDGE CASE: должен корректно обрабатывать переход через месяц")
  void monthBoundary_shouldHandleCorrectly() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-07-31T11:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-08-01T10:00:00Z"));
  }

  @Test
  @DisplayName("CUSTOM: должен корректно выполнять cron-выражение")
  void custom_shouldWorkCorrectly() {
    schedule.setScheduleType(ScheduleType.CUSTOM);
    schedule.setCronExpression("0 0 15 * * ?");
    schedule.setTargetTimezone("UTC");
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    CronTrigger trigger = new CronTrigger(schedule.getCronExpression());
    SimpleTriggerContext context = new SimpleTriggerContext();
    Instant expectedFromTrigger = trigger.nextExecution(context);

    assertThat(result).isEqualTo(expectedFromTrigger);
  }

  @Test
  @DisplayName("CUSTOM: должен выбрасывать исключение при невалидном cron-выражении")
  void custom_invalidCron_shouldThrowException() {
    schedule.setScheduleType(ScheduleType.CUSTOM);
    schedule.setCronExpression("invalid cron");
    schedule.setTargetTimezone("UTC");
    service = withFixedClock("2026-07-08T10:00:00Z");

    assertThrows(ResponseStatusException.class, () -> service.calculateNextExecution(schedule));
  }
}
