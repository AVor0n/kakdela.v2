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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyPublicResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.SurveyService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Surveys", description = "Управление опросами")
public class SurveyController {

  private final SurveyService surveyService;

  @GetMapping("/accounts/me/surveys")
  public List<SurveyShortResponseWithPermissionDto> getMySurveys(
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return surveyService.getMySurveys(currentUser.getId());
  }

  @GetMapping("/surveys/{surveyId}")
  public SurveyPublicResponseDto getPublicById(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return surveyService.getPublicById(surveyId, currentUser.getId());
  }

  @GetMapping("/surveys/{surveyId}/edit")
  public SurveyResponseDto getById(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return surveyService.getById(surveyId, currentUser.getId());
  }

  @PostMapping("/surveys")
  @ResponseStatus(HttpStatus.CREATED)
  public SurveyResponseDto create(
      @Valid @RequestBody SurveyCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return surveyService.create(currentUser.getId(), createDto);
  }

  @PutMapping("/surveys/{surveyId}")
  public SurveyResponseDto update(
      @PathVariable UUID surveyId,
      @Valid @RequestBody SurveyUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return surveyService.update(surveyId, updateDto, currentUser.getId());
  }

  @PostMapping("/surveys/{surveyId}/clone")
  @ResponseStatus(HttpStatus.CREATED)
  public SurveyResponseDto clone(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return surveyService.clone(surveyId, currentUser.getId());
  }

  @DeleteMapping("/surveys/{surveyId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    surveyService.delete(surveyId, currentUser.getId());
  }
}
