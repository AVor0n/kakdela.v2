package ru.hh.kakdela.v2.dto.survey_page;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.dto.question.QuestionResponseDto;

@AllArgsConstructor
@Getter
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
