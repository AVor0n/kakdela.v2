package ru.hh.kakdela.v2.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.dto.closing_page.ClosingPageResponseDto;
import ru.hh.kakdela.v2.dto.survey_page.SurveyPageResponseDto;

@AllArgsConstructor
@Getter
@Schema(
    name = "SurveyResponse",
    title = "DTO для получения данных опроса"
)
public class SurveyResponseDto {

  private final UUID id;
  private final UUID authorId;
  private final String title;
  private final String description;
  private final Boolean isAuthorizedOnly;
  private final Boolean isLimitedToOneResponse;
  private final Boolean doNotify;
  private final Boolean isTemplate;
  private final Boolean isPublished;
  private final Instant expireAt;
  private final LocalDateTime expireAtAtTargetTimezone;
  private final String targetTimezone;
  private final Instant createdAt;
  private final List<SurveyPageResponseDto> pages;
  private final ClosingPageResponseDto closingPage;
}
