package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import ru.hh.kakdela.v2.dto.closing.ClosingPageCreateDto;
import ru.hh.kakdela.v2.dto.closing.ClosingPageResponseDto;
import ru.hh.kakdela.v2.dto.closing.ClosingPageUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.ClosingPageService;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Closing Pages", description = "Управление завершающими страницами опросов")
public class ClosingPageController {

  private final ClosingPageService closingPageService;

  @GetMapping("/surveys/{surveyId}/closing-page")
  public ClosingPageResponseDto getBySurveyId(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return closingPageService.getBySurveyId(surveyId, currentUser.getId());
  }

  @GetMapping("/surveys/{surveyId}/closing-page/exists")
  public boolean existsBySurveyId(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return closingPageService.existsBySurveyId(surveyId, currentUser.getId());
  }

  @PostMapping("/surveys/{surveyId}/closing-page")
  @ResponseStatus(HttpStatus.CREATED)
  public ClosingPageResponseDto create(
      @PathVariable UUID surveyId,
      @Valid @RequestBody ClosingPageCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return closingPageService.create(surveyId, createDto, currentUser.getId());
  }

  @PatchMapping("/surveys/{surveyId}/closing-page")
  public ClosingPageResponseDto update(
      @PathVariable UUID surveyId,
      @Valid @RequestBody ClosingPageUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return closingPageService.update(surveyId, updateDto, currentUser.getId());
  }

  @DeleteMapping("/surveys/{surveyId}/closing-page")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    closingPageService.delete(surveyId, currentUser.getId());
  }
}
