package ru.hh.kakdela.v2.controller;

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
import ru.hh.kakdela.v2.dto.notification_schedule.NotificationScheduleCreateDto;
import ru.hh.kakdela.v2.dto.notification_schedule.NotificationScheduleResponseDto;
import ru.hh.kakdela.v2.dto.notification_schedule.NotificationScheduleUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.NotificationScheduleService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationScheduleController {

  private final NotificationScheduleService notificationScheduleService;

  @GetMapping("/surveys/{surveyId}/notifications")
  List<NotificationScheduleResponseDto> getAllBySurveyId(@PathVariable UUID surveyId) {
    return notificationScheduleService.getAllBySurveyId(surveyId);
  }

  @GetMapping("/notifications/{scheduleId}")
  NotificationScheduleResponseDto getById(@PathVariable UUID scheduleId) {
    return notificationScheduleService.getById(scheduleId);
  }

  @PostMapping("/surveys/{surveyId}/notifications")
  @ResponseStatus(HttpStatus.CREATED)
  NotificationScheduleResponseDto create(
      @PathVariable UUID surveyId,
      @Valid @RequestBody NotificationScheduleCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser
  ) {
    return notificationScheduleService.create(surveyId, createDto, authenticatedUser.getId());
  }

  @PatchMapping("notifications/{scheduleId}")
  NotificationScheduleResponseDto update(
      @PathVariable UUID scheduleId,
      @RequestBody NotificationScheduleUpdateDto dto,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser
  ) {
    return notificationScheduleService.update(scheduleId, dto, authenticatedUser.getId());
  }

  @DeleteMapping("notifications/{scheduleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID scheduleId,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser
  ) {
    notificationScheduleService.delete(scheduleId, authenticatedUser.getId());
  }
}
