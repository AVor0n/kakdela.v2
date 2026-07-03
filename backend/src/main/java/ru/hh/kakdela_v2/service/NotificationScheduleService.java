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

    NotificationSchedule notificationSchedule = NotificationSchedule.builder()
        .survey(survey)
        .name(dto.getName())
        .scheduleType(dto.getType())
        .daysOfWeek(dto.getDaysOfWeek())
        .dayOfMonth(dto.getDayOfMonth())
        .cronExpression(dto.getCronExpression())
        .executionTime(dto.getExecutionTime())
        .targetTimezone(dto.getUserTimezone())
        .isActive(dto.isActive())
        .build();

    notificationSchedule.getScheduleType().verifyType(notificationSchedule);

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

    if (dto.getType() != null) {
      dto.getType().setup(notificationSchedule, dto);
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

    notificationSchedule.setNextExecution(calculationService.calculateNextExecution(notificationSchedule));

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

}
