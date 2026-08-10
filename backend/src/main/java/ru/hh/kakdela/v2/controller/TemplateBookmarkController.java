package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.TemplateBookmarkService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Template Bookmarks", description = "Управление закладками для шаблонов")
public class TemplateBookmarkController {

  private final TemplateBookmarkService bookmarkService;
  private final SurveyMapper surveyMapper;

  @PostMapping("/templates/{templateId}/bookmark")
  @ResponseStatus(HttpStatus.CREATED)
  public void addBookmark(
      @PathVariable UUID templateId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    bookmarkService.addBookmark(templateId, currentUser.getId());
  }

  @GetMapping("/accounts/me/bookmarks")
  public List<SurveyShortResponseDto> getMyBookmarks(
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return bookmarkService.getMyBookmarks(currentUser.getId()).stream()
        .map(surveyMapper::surveyToShortDto)
        .toList();
  }

  @GetMapping("/templates/{templateId}/is-bookmarked")
  public boolean isBookmarked(
      @PathVariable UUID templateId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return bookmarkService.isBookmarked(templateId, currentUser.getId());
  }

  @DeleteMapping("/templates/{templateId}/bookmark")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBookmark(
      @PathVariable UUID templateId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    bookmarkService.delete(templateId, currentUser.getId());
  }
}
