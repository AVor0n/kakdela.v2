package ru.hh.kakdela_v2.dto.survey_page;

import lombok.Getter;
import ru.hh.kakdela_v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela_v2.model.SurveyPage;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Getter
public class SurveyPageResponseDto {

  private final UUID id;
  private final UUID surveyId;
  private final Integer serialNumber;
  private final String title;
  private final String description;
  private final List<QuestionResponseDto> questions;

  public SurveyPageResponseDto(SurveyPage page) {
    this.id = page.getId();
    this.surveyId = page.getSurvey().getId();
    this.serialNumber = page.getSerialNumber();
    this.title = page.getTitle();
    this.description = page.getDescription();
    this.questions = page.getQuestions().stream()
            .sorted(Comparator.comparingInt(q -> q.getSerialNumber()))
            .map(QuestionResponseDto::new)
            .toList();
  }
}
