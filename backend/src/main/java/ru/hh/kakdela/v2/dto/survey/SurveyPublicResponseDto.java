package ru.hh.kakdela.v2.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageShortResponseDto;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Schema(name = "Survey.PublicResponse")
public class SurveyPublicResponseDto {

  private final UUID id;
  private final AccountResponseDto author;
  private final String title;
  private final String description;
  private String attachmentUrl;
  private final Boolean isAuthorizedOnly;
  private final Boolean isLimitedToOneResponse;
  private final Instant expireAt;
  private final LocalDateTime expireAtAtTargetTimezone;
  private final String targetTimezone;
  private final List<SurveyPageShortResponseDto> pages;
  private final boolean hasCustomClosingPage;
  private final boolean hasConditions;
  private final Boolean isPublished;
}
