package ru.hh.kakdela.v2.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.dto.closing_page.ClosingPageResponseDto;
import ru.hh.kakdela.v2.dto.survey_page.SurveyPageResponseDto;

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
  private final Boolean isAuthorizedOnly;
  private final Boolean isLimitedToOneResponse;
  private final Boolean isPublished;
  private final Boolean isTemplate;
  private final Boolean doNotify;
  private final Instant expireAt;
  private final Instant createdAt;
  private final List<SurveyPageResponseDto> pages;
  private final ClosingPageResponseDto closingPage;
}
