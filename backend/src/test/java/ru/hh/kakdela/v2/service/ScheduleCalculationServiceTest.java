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
  void shouldReturnTodayWhenDailyTimeInFuture() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-07-08T09:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-08T10:00:00Z"));
  }

  @Test
  @DisplayName("DAILY: должен вернуть завтра, когда время выполнения прошло")
  void shouldReturnTomorrowWhenDailyTimePassed() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-07-08T11:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-09T10:00:00Z"));
  }

  @Test
  @DisplayName("DAILY: должен вернуть завтра, когда текущее время равно времени выполнения")
  void shouldReturnTomorrowWhenDailyTimeEqualsNow() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-09T10:00:00Z"));
  }

  @Test
  @DisplayName("DAILY: должен корректно обрабатывать выполнение в полночь")
  void shouldReturnNextDayAtMidnight() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(0, 0));
    service = withFixedClock("2026-07-08T23:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-09T00:00:00Z"));
  }

  @Test
  @DisplayName("DAILY: должен корректно обрабатывать выполнение в 23:59")
  void shouldReturnTodayAtEndOfDay() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(23, 59));
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-08T23:59:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен вернуть тот же день, когда время в будущем")
  void shouldReturnTodayWhenWeeklyTimeInFuture() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(14, 30));
    schedule.setDaysOfWeek(4);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-08T14:30:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен вернуть следующий выбранный день, когда время прошло")
  void shouldReturnNextDayWhenWeeklyTimePassed() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDaysOfWeek(21);
    service = withFixedClock("2026-07-08T15:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-10T10:00:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен перейти на следующую неделю, когда дней на неделе не осталось")
  void shouldMoveToNextWeekWhenNoDaysLeft() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDaysOfWeek(7);
    service = withFixedClock("2026-07-08T15:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-13T10:00:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен корректно обрабатывать расписание только на выходных")
  void shouldReturnNextWeekendWhenOnlyWeekendDays() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(9, 0));
    schedule.setDaysOfWeek(96);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-11T09:00:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен корректно обрабатывать расписание на один день")
  void shouldReturnCorrectDayWhenSingleDaySelected() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(9, 0));
    schedule.setDaysOfWeek(32);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-11T09:00:00Z"));
  }

  @Test
  @DisplayName("WEEKLY: должен корректно обрабатывать выполнение в воскресенье")
  void shouldReturnNextSunday() {
    schedule.setScheduleType(ScheduleType.WEEKLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDaysOfWeek(64);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-12T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен вернуть тот же месяц, когда день в будущем")
  void shouldReturnSameMonthWhenDayInFuture() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(12, 0));
    schedule.setDayOfMonth(15);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-15T12:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен вернуть следующий месяц, когда день прошёл")
  void shouldReturnNextMonthWhenDayPassed() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(5);
    service = withFixedClock("2026-07-08T15:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-08-05T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен вернуть следующий месяц, когда тот же день, но время прошло")
  void shouldReturnNextMonthWhenSameDayButTimePassed() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(8);
    service = withFixedClock("2026-07-08T15:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-08-08T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен корректно обрабатывать 31-й день, когда в месяце 31 день")
  void shouldReturnSameDayWhenMonthHas31Days() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(31);
    service = withFixedClock("2026-07-08T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-07-31T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: должен выбрасывать исключение для 31-го дня, когда в месяце 30 дней")
  void shouldThrowExceptionWhenMonthHas30Days() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(31);
    service = withFixedClock("2026-04-15T10:00:00Z");

    assertThrows(ResponseStatusException.class, () -> service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("MONTHLY: должен корректно обрабатывать 29 февраля в високосный год")
  void shouldReturnFeb29WhenLeapYear() {
    schedule.setScheduleType(ScheduleType.MONTHLY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setDayOfMonth(29);
    service = withFixedClock("2024-02-15T10:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2024-02-29T10:00:00Z"));
  }

  @Test
  @DisplayName("MONTHLY: для 29 февраля в невисокосный год - выбрасывает исключение в феврале")
  void shouldThrowExceptionWhenNonLeapYearInFebruary() {
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
  void shouldWorkCorrectlyForDifferentTimezones(String timezone, String now, String expected) {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setTargetTimezone(timezone);
    service = withFixedClock(now);

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse(expected));
  }

  @Test
  @DisplayName("EDGE CASE: должен корректно обрабатывать невалидный часовой пояс")
  void shouldThrowExceptionWhenTimezoneInvalid() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    schedule.setTargetTimezone("Invalid/Timezone");
    service = withFixedClock("2026-07-08T09:00:00Z");

    assertThrows(DateTimeException.class, () -> service.calculateNextExecution(schedule));
  }

  @Test
  @DisplayName("EDGE CASE: должен корректно обрабатывать переход через год")
  void shouldHandleYearBoundaryCorrectly() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-12-31T11:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2027-01-01T10:00:00Z"));
  }

  @Test
  @DisplayName("EDGE CASE: должен корректно обрабатывать переход через месяц")
  void shouldHandleMonthBoundaryCorrectly() {
    schedule.setScheduleType(ScheduleType.DAILY);
    schedule.setExecutionTime(LocalTime.of(10, 0));
    service = withFixedClock("2026-07-31T11:00:00Z");

    Instant result = service.calculateNextExecution(schedule);

    assertThat(result).isEqualTo(Instant.parse("2026-08-01T10:00:00Z"));
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

    assertThat(result).isEqualTo(expectedFromTrigger);
  }

  @Test
  @DisplayName("CUSTOM: должен выбрасывать исключение при невалидном cron-выражении")
  void shouldThrowExceptionWhenCronInvalid() {
    schedule.setScheduleType(ScheduleType.CUSTOM);
    schedule.setCronExpression("invalid cron");
    schedule.setTargetTimezone("UTC");
    service = withFixedClock("2026-07-08T10:00:00Z");

    assertThrows(ResponseStatusException.class, () -> service.calculateNextExecution(schedule));
  }
}
