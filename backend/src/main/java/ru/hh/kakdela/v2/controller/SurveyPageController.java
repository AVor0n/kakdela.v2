package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageCreateDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageResponseDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.SurveyPageService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Pages", description = "Управление страницами опросов")
public class SurveyPageController {

  private final SurveyPageService surveyPageService;

  //  @GetMapping("/surveys/{surveyId}/pages")
  //  public List<SurveyPageResponseDto> getAllBySurveyId(@PathVariable UUID surveyId) {
  //    return surveyPageService.getAllBySurveyId(surveyId);
  //  }

  @GetMapping("/pages/{pageId}")
  public SurveyPageResponseDto getById(
      @PathVariable UUID pageId,
      @RequestParam UUID responseId
  ) {
    return surveyPageService.getById(pageId, responseId);
  }

  @PostMapping("/surveys/{surveyId}/pages")
  @ResponseStatus(HttpStatus.CREATED)
  public SurveyPageResponseDto create(
      @PathVariable UUID surveyId,
      @Valid @RequestBody SurveyPageCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return surveyPageService.create(surveyId, createDto, currentUser.getId());
  }

  @PutMapping("/pages/{pageId}")
  public SurveyPageResponseDto update(
      @PathVariable UUID pageId,
      @Valid @RequestBody SurveyPageUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return surveyPageService.update(pageId, updateDto, currentUser.getId());
  }

  @DeleteMapping("/pages/{pageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID pageId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    surveyPageService.delete(pageId, currentUser.getId());
  }
}
