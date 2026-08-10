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
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.template.TemplateResponseDto;
import ru.hh.kakdela.v2.dto.template.TemplateUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.TemplateService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Templates", description = "Управление шаблонами опросов")
public class TemplateController {

  private final TemplateService templateService;

  @PostMapping("/surveys/{surveyId}/create-template")
  @ResponseStatus(HttpStatus.CREATED)
  public TemplateResponseDto createTemplate(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return templateService.createTemplate(surveyId, currentUser.getId());
  }

  @GetMapping("/templates")
  public List<TemplateResponseDto> getPublicTemplates() {
    return templateService.getPublicTemplates();
  }

  @GetMapping("/templates/{templateId}")
  public TemplateResponseDto getTemplate(
      @PathVariable UUID templateId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return templateService.getTemplate(templateId, currentUser.getId());
  }

  @GetMapping("/accounts/me/templates")
  public List<TemplateResponseDto> getMyTemplates(
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return templateService.getMyTemplates(currentUser.getId());
  }

  @PatchMapping("/templates/{templateId}")
  public TemplateResponseDto updateTemplate(
      @PathVariable UUID templateId,
      @Valid @RequestBody TemplateUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return templateService.updateTemplate(templateId, updateDto, currentUser.getId());
  }

  @PostMapping("/templates/{templateId}/create-survey")
  @ResponseStatus(HttpStatus.CREATED)
  public SurveyResponseDto createSurveyFromTemplate(
      @PathVariable UUID templateId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return templateService.createSurveyFromTemplate(templateId, currentUser.getId());
  }

  @DeleteMapping("/templates/{templateId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTemplate(
      @PathVariable UUID templateId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    templateService.deleteTemplate(templateId, currentUser.getId());
  }
}
