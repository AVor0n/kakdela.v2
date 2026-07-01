package ru.hh.kakdela_v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.NotificationSchedule;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationScheduleDaoImpl implements NotificationScheduleDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<NotificationSchedule> findAll() {
    return entityManager
        .createQuery("FROM NotificationSchedule", NotificationSchedule.class)
        .getResultList();
  }

  @Override
  public Optional<NotificationSchedule> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(NotificationSchedule.class, id));
  }

  @Override
  public List<NotificationSchedule> findAllBySurveyId(UUID surveyId) {
    return entityManager
        .createQuery("FROM NotificationSchedule ns WHERE ns.survey.id = :surveyId", NotificationSchedule.class)
        .setParameter("surveyId", surveyId)
        .getResultList();
  }

  @Override
  public List<NotificationSchedule> findByIsActiveTrueAndNextExecutionBefore(Instant now) {
    return entityManager
        .createQuery(
            "FROM NotificationSchedule ns WHERE ns.isActive = true AND ns.nextExecution < :now",
            NotificationSchedule.class)
        .setParameter("now", now)
        .getResultList();
  }

  @Override
  public void save(NotificationSchedule notificationSchedule) {
    entityManager.persist(notificationSchedule);
  }

  @Override
  public void update(NotificationSchedule notificationSchedule) {
    entityManager.merge(notificationSchedule);
  }

  @Override
  public void delete(NotificationSchedule notificationSchedule) {
    entityManager.remove(notificationSchedule);
  }
}
