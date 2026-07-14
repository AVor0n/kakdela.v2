package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.permission.PermissionRequestDto;
import ru.hh.kakdela.v2.dto.permission.PermissionResponseDto;
import ru.hh.kakdela.v2.dto.permission.PermissionUpdateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.PermissionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Управление правами пользователей в опросе")
public class PermissionController {

  private final PermissionService permissionService;
  private final SurveyMapper surveyMapper;

  @GetMapping("surveys/{surveyId}/permissions")
  public List<PermissionResponseDto> getAllBySurveyId(@PathVariable UUID surveyId) {
    return permissionService.getAllBySurveyId(surveyId);
  }

  @GetMapping("accounts/me/permissions")
  public List<PermissionResponseDto> getAllByAccountId(
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return permissionService.getAllByAccountId(currentUser.getId());
  }

  @PostMapping("surveys/{surveyId}/permissions")
  @ResponseStatus(HttpStatus.CREATED)
  public PermissionResponseDto create(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @RequestBody PermissionRequestDto dto
  ) {
    return permissionService.create(surveyId, currentUser.getId(), dto);
  }

  @PutMapping("surveys/{surveyId}/permissions/{accountId}")
  public PermissionResponseDto updateFull(
      @PathVariable UUID surveyId,
      @PathVariable UUID accountId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @RequestBody PermissionRequestDto dto
  ) {
    return permissionService.updateFull(surveyId, accountId, currentUser.getId(), dto);
  }

  @PatchMapping("surveys/{surveyId}/permissions/{accountId}")
  public PermissionResponseDto updatePartial(
      @PathVariable UUID surveyId,
      @PathVariable UUID accountId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @RequestBody PermissionUpdateDto dto
  ) {
    return permissionService.updatePartial(surveyId, accountId, currentUser.getId(), dto);
  }

  @DeleteMapping("surveys/{surveyId}/permissions/{accountId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID surveyId,
      @PathVariable UUID accountId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    permissionService.delete(surveyId, accountId, currentUser.getId());
  }
}
