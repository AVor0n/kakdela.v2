package ru.hh.kakdela_v2.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.hh.kakdela_v2.model.NotificationSchedule;
import ru.hh.kakdela_v2.service.NotificationScheduleService;
import ru.hh.kakdela_v2.service.NotificationService;
import ru.hh.kakdela_v2.service.ScheduleCalculationService;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicNotificationScheduler {

  private final NotificationScheduleService notificationScheduleService;
  private final ScheduleCalculationService calculationService;
  private final NotificationService notificationService;
  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
  private final Map<UUID, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

  @Scheduled(fixedDelay = 60000)
  public void refreshSchedules() {
    Instant now = Instant.now();

    log.info("start notifications check");
    // Находим задачи, которые должны выполниться
    List<NotificationSchedule> dueSchedules = notificationScheduleService
        .getAllEntityByIsActiveTrueAndNextExecutionBefore(now);
    log.info("find {} tasks", dueSchedules.size());

    for (NotificationSchedule schedule : dueSchedules) {
      if (!scheduledTasks.containsKey(schedule.getId())) {
        // Выполняем сразу в отдельном потоке
        executorService.submit(() -> executeTask(schedule));
      }
    }
  }

  private void scheduleTask(NotificationSchedule schedule) {
    // Отменяем старую задачу
    ScheduledFuture<?> existing = scheduledTasks.remove(schedule.getId());
    if (existing != null) {
      existing.cancel(false);
    }

    // Если nextExecution не задан или уже прошел - вычисляем
    Instant now = Instant.now();
    Instant nextExecution = schedule.getNextExecution();

    if (nextExecution == null || nextExecution.isBefore(now)) {
      nextExecution = calculationService.calculateNextExecution(schedule);
      schedule.setNextExecution(nextExecution);
      notificationScheduleService.updateByEntity(schedule);
    }

    // Планируем задачу
    long delay = Duration.between(now, nextExecution).toMillis();

    if (delay < 0) {
      delay = 0;
    }

    ScheduledFuture<?> future = executorService.schedule(
        () -> executeTask(schedule),
        delay,
        TimeUnit.MILLISECONDS
    );

    scheduledTasks.put(schedule.getId(), future);

    String nextExecutionStr = nextExecution
        .atZone(ZoneId.of(schedule.getUserTimezone()))
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z"));

    log.info("Запланирована задача '{}' на {}", schedule.getName(), nextExecutionStr);
  }

  private void executeTask(NotificationSchedule schedule) {
    try {
      log.info("Выполнение задачи: {}", schedule.getName());

      // Обновляем lastExecution
      schedule.setLastExecution(Instant.now());

      // Вычисляем следующее выполнение
      Instant nextExecution = calculationService.calculateNextExecution(schedule);
      schedule.setNextExecution(nextExecution);

      notificationScheduleService.updateByEntity(schedule);

      notificationService.sendNotificationForUsersWithUncompletedResponse(schedule.getSurvey().getId());
      log.info("Отправлены уведомления для задачи '{}'", schedule.getName());

    } catch (Exception e) {
      log.error("Ошибка при выполнении задачи '{}'", schedule.getName(), e);
    } finally {
      scheduledTasks.remove(schedule.getId());

      // Планируем следующее выполнение (если задача активна)
      if (schedule.getIsActive()) {
        scheduleTask(schedule);
      }
    }
  }
}
