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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.permission.PermissionCreateDto;
import ru.hh.kakdela.v2.dto.permission.PermissionResponseDto;
import ru.hh.kakdela.v2.dto.permission.PermissionUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.PermissionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Управление правами пользователей в опросе")
public class PermissionController {

  private final PermissionService permissionService;

  @GetMapping("surveys/{surveyId}/permissions")
  public List<PermissionResponseDto> getAllBySurveyId(@PathVariable UUID surveyId) {
    return permissionService.getAllBySurveyId(surveyId);
  }

  @PostMapping("surveys/{surveyId}/permissions")
  @ResponseStatus(HttpStatus.CREATED)
  public PermissionResponseDto create(
      @PathVariable UUID surveyId,
      @Valid @RequestBody PermissionCreateDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return permissionService.create(surveyId, dto, currentUser.getId());
  }

  @PutMapping("surveys/{surveyId}/permissions")
  public PermissionResponseDto updateFull(
      @PathVariable UUID surveyId,
      @RequestParam UUID accountId,
      @Valid @RequestBody PermissionUpdateDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return permissionService.updateFull(surveyId, accountId, dto, currentUser.getId());
  }

  @DeleteMapping("surveys/{surveyId}/permissions")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID surveyId,
      @RequestParam UUID accountId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    permissionService.delete(surveyId, accountId, currentUser.getId());
  }
}
