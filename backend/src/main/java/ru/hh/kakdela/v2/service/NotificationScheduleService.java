package ru.hh.kakdela.v2.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.NotificationScheduleDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.notification.schedule.NotificationScheduleCreateDto;
import ru.hh.kakdela.v2.dto.notification.schedule.NotificationScheduleResponseDto;
import ru.hh.kakdela.v2.dto.notification.schedule.NotificationScheduleUpdateDto;
import ru.hh.kakdela.v2.mapper.NotificationScheduleMapper;
import ru.hh.kakdela.v2.model.NotificationSchedule;
import ru.hh.kakdela.v2.model.Survey;

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
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Опрос " + surveyId + " не найден"
        ));

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
    }

    return notificationScheduleDao.findAllBySurveyId(surveyId).stream()
        .map(mapper::notificationScheduleToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationSchedule> getAllEntityByIsActiveTrueAndNextExecutionBefore(Instant now) {
    return notificationScheduleDao.findByIsActiveTrueAndNextExecutionBefore(now).stream()
        .filter(schedule -> !schedule.getSurvey().isTemplate())
        .toList();
  }

  @Transactional
  public NotificationScheduleResponseDto create(
      UUID surveyId,
      NotificationScheduleCreateDto dto,
      UUID accountId
  ) {
    permissionService.checkCanEdit(surveyId, accountId);

    Survey survey = surveyDao.findById(surveyId).orElseThrow(
        () -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Опрос " + surveyId + " не найден, нельзя задать расписание."
        )
    );

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
    }

    NotificationSchedule notificationSchedule = NotificationSchedule.builder()
        .id(UUID.randomUUID())
        .survey(survey)
        .name(dto.getName())
        .scheduleType(dto.getType())
        .daysOfWeek(dto.getDaysOfWeek())
        .dayOfMonth(dto.getDayOfMonth())
        .cronExpression(dto.getCronExpression())
        .executionTime(dto.getExecutionTime())
        .targetTimezone(dto.getTargetTimezone())
        .isActive(dto.isActive())
        .build();

    notificationSchedule.getScheduleType().verifyType(notificationSchedule);

    notificationSchedule.setNextExecution(
        calculationService.calculateNextExecution(notificationSchedule));

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

    Survey survey = notificationSchedule.getSurvey();

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
    }

    permissionService.checkCanEdit(
        notificationSchedule.getSurvey().getId(), accountId);

    if (dto.getName() != null) {
      notificationSchedule.setName(dto.getName());
    }

    if (dto.getExecutionTime() != null) {
      notificationSchedule.setExecutionTime(dto.getExecutionTime());
    }

    if (dto.getDaysOfWeek() != null) {
      notificationSchedule.setDaysOfWeek(dto.getDaysOfWeek());
    }

    if (dto.getDayOfMonth() != null) {
      notificationSchedule.setDayOfMonth(dto.getDayOfMonth());
    }

    if (dto.getCronExpression() != null) {
      notificationSchedule.setCronExpression(dto.getCronExpression());
    }

    if (dto.getType() != null) {
      notificationSchedule.setScheduleType(dto.getType());
    }

    if (dto.getTargetTimezone() != null) {
      notificationSchedule.setTargetTimezone(dto.getTargetTimezone());
    }

    if (dto.getIsActive() != null) {
      notificationSchedule.setIsActive(dto.getIsActive());
    }

    notificationSchedule.getScheduleType().setup(notificationSchedule);

    notificationSchedule.setNextExecution(
        calculationService.calculateNextExecution(notificationSchedule));

    notificationScheduleDao.update(notificationSchedule);
    return mapper.notificationScheduleToDto(notificationSchedule);
  }

  @Transactional
  public void updateByEntity(NotificationSchedule notificationSchedule) {
    if (notificationSchedule.getSurvey().isTemplate()) {
      log.warn("Попытка обновления расписания для шаблона id={}",
          notificationSchedule.getSurvey().getId());
      return;
    }

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

    Survey survey = toDelete.getSurvey();

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
    }

    permissionService.checkCanEdit(toDelete.getSurvey().getId(), accountId);

    notificationScheduleDao.delete(toDelete);
  }

}
