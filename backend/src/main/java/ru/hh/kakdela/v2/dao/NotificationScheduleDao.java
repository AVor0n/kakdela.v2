package ru.hh.kakdela.v2.dao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.NotificationSchedule;

public interface NotificationScheduleDao {
  List<NotificationSchedule> findAll();

  Optional<NotificationSchedule> findById(UUID id);

  List<NotificationSchedule> findAllBySurveyId(UUID surveyId);

  List<NotificationSchedule> findByIsActiveTrueAndNextExecutionBefore(Instant now);

  void save(NotificationSchedule notificationSchedule);

  void update(NotificationSchedule notificationSchedule);

  void delete(NotificationSchedule notificationSchedule);
}
