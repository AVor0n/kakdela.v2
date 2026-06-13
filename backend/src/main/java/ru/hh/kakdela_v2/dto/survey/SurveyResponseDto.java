package ru.hh.kakdela_v2.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.dto.closing_page.ClosingPageResponseDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageResponseDto;
import ru.hh.kakdela_v2.model.Survey;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class SurveyResponseDto {

  private final UUID id;
  private final UUID authorId;
  private final String title;
  private final String description;
  private final boolean isAuthorizedOnly;
  private final boolean isLimitedToOneResponse;
  private final boolean isPublished;
  private final boolean isTemplate;
  private final boolean doNotify;
  private final Instant expireAt;
  private final Instant createdAt;
  private final List<SurveyPageResponseDto> pages;
  private final ClosingPageResponseDto closingPage;

  public SurveyResponseDto(Survey survey) {
    this.id = survey.getId();
    this.authorId = survey.getAuthor().getId();
    this.title = survey.getTitle();
    this.description = survey.getDescription();
    this.isAuthorizedOnly = survey.isAuthorizedOnly();
    this.isLimitedToOneResponse = survey.isLimitedToOneResponse();
    this.isPublished = survey.isPublished();
    this.isTemplate = survey.isTemplate();
    this.doNotify = survey.isDoNotify();
    this.expireAt = survey.getExpireAt();
    this.createdAt = survey.getCreatedAt();
    this.pages = survey.getPages().stream()
        .map(SurveyPageResponseDto::new)
        .toList();
    this.closingPage = survey.getClosingPage() != null
        ? new ClosingPageResponseDto(survey.getClosingPage())
        : null;
  }
}
