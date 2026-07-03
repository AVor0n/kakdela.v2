package ru.hh.kakdela.v2.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
@AllArgsConstructor
@Getter
@Schema(
    name = "SurveyShortResponseDto",
    title = "Краткий DTO данных опроса"
)
public class SurveyShortResponseDto {

  private final UUID id;
  private final String title;
  private final String description;
  private final Boolean isPublished;
  private final Instant createdAt;
}
