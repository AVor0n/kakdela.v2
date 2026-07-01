package ru.hh.kakdela_v2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela_v2.dto.notification_schedule.NotificationScheduleCreateDto;
import ru.hh.kakdela_v2.dto.notification_schedule.NotificationScheduleResponseDto;
import ru.hh.kakdela_v2.dto.notification_schedule.NotificationScheduleUpdateDto;
import ru.hh.kakdela_v2.security.CustomUserDetails;
import ru.hh.kakdela_v2.service.NotificationScheduleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationScheduleController {

  private final NotificationScheduleService notificationScheduleService;

  @GetMapping("/surveys/{surveyId}/notifications")
  List<NotificationScheduleResponseDto> getAllBySurveyId (@PathVariable UUID surveyId) {
    return notificationScheduleService.getAllBySurveyId(surveyId);
  }

  @GetMapping("/notifications/{notificationId}")
  NotificationScheduleResponseDto getById (@PathVariable UUID id) {
    return notificationScheduleService.getById(id);
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
