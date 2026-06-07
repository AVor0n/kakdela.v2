package ru.hh.kakdela_v2.dto.survey;

import lombok.Getter;
import ru.hh.kakdela_v2.model.Survey;

import java.time.Instant;
import java.util.UUID;

@Getter
public class SurveyShortResponseDto {

  private final UUID id;
  private final String title;
  private final String description;
  private final boolean isPublished;
  private final Instant createdAt;

  public SurveyShortResponseDto(Survey survey) {
    this.id = survey.getId();
    this.title = survey.getTitle();
    this.description = survey.getDescription();
    this.isPublished = survey.isPublished();
    this.createdAt = survey.getCreatedAt();
  }
}
