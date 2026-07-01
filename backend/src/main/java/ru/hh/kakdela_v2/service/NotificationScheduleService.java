package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.NotificationScheduleDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dto.notification_schedule.NotificationScheduleCreateDto;
import ru.hh.kakdela_v2.dto.notification_schedule.NotificationScheduleResponseDto;
import ru.hh.kakdela_v2.dto.notification_schedule.NotificationScheduleUpdateDto;
import ru.hh.kakdela_v2.mapper.NotificationScheduleMapper;
import ru.hh.kakdela_v2.model.NotificationSchedule;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Survey;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationScheduleService {

  private final NotificationScheduleDao notificationScheduleDao;
  private final SurveyDao surveyDao;
  private final NotificationScheduleMapper mapper;
  private final PermissionService permissionService;
  private final ScheduleCalculationService calculationService;

  @Transactional(readOnly = true)
  public NotificationScheduleResponseDto getById(UUID id) {
    NotificationSchedule notificationSchedule = notificationScheduleDao.findById(id).orElseThrow(
        () -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Не найдено расписание уведомлений " + id
        )
    );

    return mapper.notificationScheduleToDto(notificationSchedule);
  }

  @Transactional(readOnly = true)
  public List<NotificationScheduleResponseDto> getAllBySurveyId(UUID surveyId) {
    return notificationScheduleDao.findAllBySurveyId(surveyId).stream()
        .map(mapper::notificationScheduleToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationSchedule> getAllEntityByIsActiveTrueAndNextExecutionBefore(Instant now) {
    return notificationScheduleDao.findByIsActiveTrueAndNextExecutionBefore(now);
  }

  @Transactional
  public NotificationScheduleResponseDto create(
      UUID surveyId,
      NotificationScheduleCreateDto dto,
      UUID accountId
  ) {
    permissionService.checkAccess(surveyId, accountId, Permission.SurveyRole.EDITOR);
    Survey survey = surveyDao.findById(surveyId).orElseThrow(
        () -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Опрос " + surveyId + " не найден, нельзя задать расписание."
        )
    );

    NotificationSchedule.ScheduleType type = determineScheduleType(
        dto.getDaysOfWeek(),
        dto.getDayOfMonth(),
        dto.getCronExpression()
    );

    NotificationSchedule notificationSchedule = NotificationSchedule.builder()
        .survey(survey)
        .name(dto.getName())
        .scheduleType(type)
        .daysOfWeek(dto.getDaysOfWeek())
        .dayOfMonth(dto.getDayOfMonth())
        .cronExpression(dto.getCronExpression())
        .executionTime(dto.getExecutionTime())
        .userTimezone(dto.getUserTimezone())
        .isActive(dto.isActive())
        .build();

    notificationSchedule.setNextExecution(calculationService.calculateNextExecution(notificationSchedule));
    notificationScheduleDao.save(notificationSchedule);

    log.info("Расписание уведомлений {} сохранено", dto.getName());
    return mapper.notificationScheduleToDto(notificationSchedule);
  }

  @Transactional
  public NotificationScheduleResponseDto update(
      UUID id,
      NotificationScheduleUpdateDto dto,
      UUID accountId
  ) {
    NotificationSchedule notificationSchedule = notificationScheduleDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Опрос " + id + " не найден"
            )
        );
    permissionService.checkAccess(
        notificationSchedule.getSurvey().getId(),
        accountId,
        Permission.SurveyRole.EDITOR
    );

    if (dto.getName() != null) {
      notificationSchedule.setName(dto.getName());
    }

    switch (dto.getType()) {
      case DAILY:
        notificationSchedule.setDaysOfWeek(null);
        notificationSchedule.setDayOfMonth(null);
        notificationSchedule.setCronExpression(null);
        notificationSchedule.setScheduleType(NotificationSchedule.ScheduleType.DAILY);
        break;
      case WEEKLY:
        if (dto.getDaysOfWeek() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Дни недели должны быть указаны для этого типа уведомлений"
          );
        }
        notificationSchedule.setDaysOfWeek(dto.getDaysOfWeek());
        notificationSchedule.setDayOfMonth(null);
        notificationSchedule.setCronExpression(null);
        notificationSchedule.setScheduleType(NotificationSchedule.ScheduleType.WEEKLY);
      case MONTHLY:
        if (dto.getDayOfMonth() == null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Число месяца должно быть указано для этого типа уведомлений"
          );
        }
        notificationSchedule.setDaysOfWeek(null);
        notificationSchedule.setDayOfMonth(dto.getDayOfMonth());
        notificationSchedule.setCronExpression(null);
        notificationSchedule.setScheduleType(NotificationSchedule.ScheduleType.MONTHLY);
      case CUSTOM:
        String cronExpression = dto.getCronExpression();
        if (cronExpression == null || cronExpression.isBlank()) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Cron-выражение должно быть указано для этого типа уведомлений"
          );
        }
        notificationSchedule.setDaysOfWeek(null);
        notificationSchedule.setDayOfMonth(null);
        notificationSchedule.setCronExpression(cronExpression);
        notificationSchedule.setScheduleType(NotificationSchedule.ScheduleType.CUSTOM);
    }

    if (dto.getExecutionTime() != null) {
      notificationSchedule.setExecutionTime(dto.getExecutionTime());
    }

    if (dto.getUserTimezone() != null) {
      notificationSchedule.setExecutionTime(dto.getExecutionTime());
    }

    if (dto.getIsActive() != null) {
      notificationSchedule.setIsActive(dto.getIsActive());
    }

    notificationScheduleDao.update(notificationSchedule);
    return mapper.notificationScheduleToDto(notificationSchedule);
  }

  @Transactional
  public void updateByEntity(NotificationSchedule notificationSchedule) {
    notificationScheduleDao.update(notificationSchedule);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    NotificationSchedule toDelete = notificationScheduleDao.findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Опрос " + id + " не найден"
                )
            );
    permissionService.checkAccess(toDelete.getSurvey().getId(), accountId, Permission.SurveyRole.EDITOR);

    notificationScheduleDao.delete(toDelete);
  }

  private NotificationSchedule.ScheduleType determineScheduleType(
      Integer daysOfWeek,
      Integer dayOfMonth,
      String cronExpression
  ) {
    NotificationSchedule.ScheduleType type = null;
    if (daysOfWeek != null) {
      type = NotificationSchedule.ScheduleType.WEEKLY;
    }
    if (dayOfMonth != null) {
      if (type != null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Уведомление не может иметь несколько типов одновременно"
        );
      }
      type = NotificationSchedule.ScheduleType.MONTHLY;
    }
    if (cronExpression != null) {
      if (type != null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Уведомление не может иметь несколько типов одновременно"
        );
      }
      type = NotificationSchedule.ScheduleType.CUSTOM;
    }
    if (type == null) {
      type = NotificationSchedule.ScheduleType.DAILY;
    }

    return type;
  }

}
