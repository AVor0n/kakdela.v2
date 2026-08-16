package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.notification.schedule.NotificationScheduleCreateDto;
import ru.hh.kakdela.v2.dto.notification.schedule.NotificationScheduleResponseDto;
import ru.hh.kakdela.v2.dto.notification.schedule.NotificationScheduleUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.NotificationScheduleService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Notification Schedules", description = "Управление расписаниями напоминаний")
public class NotificationScheduleController {

  private final NotificationScheduleService notificationScheduleService;

  @GetMapping("/surveys/{surveyId}/notifications")
  List<NotificationScheduleResponseDto> getAllBySurveyId(@PathVariable UUID surveyId) {
    return notificationScheduleService.getAllBySurveyId(surveyId);
  }

  @GetMapping("/notifications/{notificationId}")
  NotificationScheduleResponseDto getById(@PathVariable UUID notificationId) {
    return notificationScheduleService.getById(notificationId);
  }

  @PostMapping("/surveys/{surveyId}/notifications")
  @ResponseStatus(HttpStatus.CREATED)
  NotificationScheduleResponseDto create(
      @PathVariable UUID surveyId,
      @Valid @RequestBody NotificationScheduleCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return notificationScheduleService.create(surveyId, createDto, currentUser.getId());
  }

  @PatchMapping("notifications/{notificationId}")
  NotificationScheduleResponseDto update(
      @PathVariable UUID notificationId,
      @RequestBody NotificationScheduleUpdateDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return notificationScheduleService.update(notificationId, dto, currentUser.getId());
  }

  @DeleteMapping("notifications/{notificationId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    notificationScheduleService.delete(notificationId, currentUser.getId());
  }
}
