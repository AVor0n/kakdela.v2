package ru.hh.kakdela_v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.hh.kakdela_v2.validator.NullOrNotBlank;

import java.time.Instant;
import java.time.LocalTime;
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
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
  }
}
