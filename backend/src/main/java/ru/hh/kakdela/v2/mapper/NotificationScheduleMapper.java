package ru.hh.kakdela.v2.mapper;

import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.notification_schedule.NotificationScheduleResponseDto;
import ru.hh.kakdela.v2.model.NotificationSchedule;

@Component
public class NotificationScheduleMapper {
  public NotificationScheduleResponseDto notificationScheduleToDto(NotificationSchedule object) {
    return new NotificationScheduleResponseDto(
        object.getId(),
        object.getSurvey().getId(),
        object.getScheduleType(),
        object.getDaysOfWeek(),
        object.getDayOfMonth(),
        object.getCronExpression(),
        object.getExecutionTime(),
        object.getTargetTimezone(),
        object.getIsActive()
    );
  }
}
