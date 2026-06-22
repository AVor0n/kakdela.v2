package ru.hh.kakdela_v2.dto.survey_page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela_v2.model.Question;
import ru.hh.kakdela_v2.model.SurveyPage;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class SurveyPageResponseDto {

  private final UUID id;
  private final UUID surveyId;
  private final int serialNumber;
  private final String title;
  private final String description;
  private final List<QuestionResponseDto> questions;
}
