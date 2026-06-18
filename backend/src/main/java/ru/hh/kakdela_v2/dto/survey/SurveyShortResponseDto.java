package ru.hh.kakdela_v2.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.model.Survey;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class SurveyShortResponseDto {

  private final UUID id;
  private final String title;
  private final String description;
  private final Boolean isPublished;
  private final Instant createdAt;
}
