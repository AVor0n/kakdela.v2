package ru.hh.kakdela.v2.dto.survey_page;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.hh.kakdela.v2.dto.question.QuestionResponseDto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Schema(
    name = "SurveyPageResponse",
    title = "DTO для получения данных страницы опроса"
)
public class SurveyPageResponseDto {

  private final UUID id;
  private final UUID surveyId;
  private final int serialNumber;
  private final String title;
  private final String description;
  private final List<QuestionResponseDto> questions;
}
