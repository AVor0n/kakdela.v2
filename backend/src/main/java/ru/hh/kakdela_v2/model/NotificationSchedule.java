package ru.hh.kakdela_v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dto.notification_schedule.NotificationScheduleCreateDto;
import ru.hh.kakdela_v2.dto.notification_schedule.NotificationScheduleUpdateDto;
import ru.hh.kakdela_v2.validator.NullOrNotBlank;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Entity
@Table(name = "notification_schedule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSchedule {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Survey survey;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "schedule_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private ScheduleType scheduleType;

  @Column(name = "days_of_week")
  private Integer daysOfWeek;

  @Column(name = "day_of_month")
  private Integer dayOfMonth;

  @Column(name = "cron_expression")
  private String cronExpression;

  @Column(name = "execution_time", nullable = false)
  private LocalTime executionTime;

  @Column(name = "user_timezone")
  @Builder.Default
  private String userTimezone = "Europe/Moscow";

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "next_execution")
  private Instant nextExecution;

  @Column(name = "last_execution")
  private Instant lastExecution;

  public enum ScheduleType {
    DAILY {
      @Override
      public NotificationSchedule setup(NotificationSchedule schedule, NotificationScheduleUpdateDto dto) {
        schedule.setDaysOfWeek(null);
        schedule.setDayOfMonth(null);
        schedule.setCronExpression(null);
        schedule.setScheduleType(NotificationSchedule.ScheduleType.DAILY);
        return schedule;
      }

      @Override
      public void verifyType(NotificationSchedule schedule) {
        if (schedule.getExecutionTime() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Должно быть указано время выполнения"
          );
        }
      }

      @Override
      public ZonedDateTime findNext(NotificationSchedule schedule,
                                    ZonedDateTime candidate,
                                    ZonedDateTime now) {
        return candidate.isAfter(now) ? candidate : candidate.plusDays(1);
      }
    },
    WEEKLY {
      @Override
      public NotificationSchedule setup(NotificationSchedule schedule, NotificationScheduleUpdateDto dto) {
        if (dto.getDaysOfWeek() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Дни недели должны быть указаны для этого типа уведомлений"
          );
        }
        schedule.setDaysOfWeek(dto.getDaysOfWeek());
        schedule.setDayOfMonth(null);
        schedule.setCronExpression(null);
        schedule.setScheduleType(NotificationSchedule.ScheduleType.WEEKLY);
        return schedule;
      }

      @Override
      public void verifyType(NotificationSchedule schedule) {
        if (schedule.getExecutionTime() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Должно быть указано время выполнения"
          );
        }
        if (schedule.getDaysOfWeek() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Дни недели должны быть указаны для этого типа уведомлений"
          );
        }
      }

      @Override
      public ZonedDateTime findNext(NotificationSchedule schedule,
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
    },
    MONTHLY {
      @Override
      public NotificationSchedule setup(NotificationSchedule schedule, NotificationScheduleUpdateDto dto) {
        if (dto.getDayOfMonth() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Число месяца должно быть указано для этого типа уведомлений"
          );
        }
        schedule.setDaysOfWeek(null);
        schedule.setDayOfMonth(dto.getDayOfMonth());
        schedule.setCronExpression(null);
        schedule.setScheduleType(NotificationSchedule.ScheduleType.MONTHLY);
        return schedule;
      }

      @Override
      public void verifyType(NotificationSchedule schedule) {
        if (schedule.getExecutionTime() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Должно быть указано время выполнения"
          );
        }
        if (schedule.getDayOfMonth() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Число месяца должно быть указано для этого типа уведомлений"
          );
        }
      }

      @Override
      public ZonedDateTime findNext(NotificationSchedule schedule,
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
    },
    CUSTOM {
      @Override
      public NotificationSchedule setup(NotificationSchedule schedule, NotificationScheduleUpdateDto dto) {
        String cronExpression = dto.getCronExpression();
        if (cronExpression == null || cronExpression.isBlank()) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Cron-выражение должно быть указано для этого типа уведомлений"
          );
        }
        schedule.setDaysOfWeek(null);
        schedule.setDayOfMonth(null);
        schedule.setCronExpression(cronExpression);
        schedule.setScheduleType(NotificationSchedule.ScheduleType.CUSTOM);
        return schedule;
      }

      @Override
      public void verifyType(NotificationSchedule schedule) {
        if (schedule.getCronExpression() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Cron-выражение должно быть указано для этого типа уведомлений"
          );
        }
      }

      @Override
      public ZonedDateTime findNext(NotificationSchedule schedule,
                                    ZonedDateTime candidate,
                                    ZonedDateTime now) {
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
    };

    private static final Logger log = LoggerFactory.getLogger(ScheduleType.class);
    public abstract NotificationSchedule setup(NotificationSchedule schedule, NotificationScheduleUpdateDto dto);
    public abstract void verifyType(NotificationSchedule schedule);
    public abstract ZonedDateTime findNext(NotificationSchedule schedule,
                                           ZonedDateTime candidate,
                                           ZonedDateTime now);
  }
}
