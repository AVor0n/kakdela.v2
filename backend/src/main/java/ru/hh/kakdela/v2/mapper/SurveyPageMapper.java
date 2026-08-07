package ru.hh.kakdela.v2.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPagePublicResponseDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageResponseDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageShortResponseDto;
import ru.hh.kakdela.v2.model.SurveyPage;

@Component
@RequiredArgsConstructor
public class SurveyPageMapper {

  private final QuestionMapper questionMapper;

  public SurveyPageResponseDto surveyPageToDto(SurveyPage surveyPage) {
    return new SurveyPageResponseDto(
        surveyPage.getId(),
        surveyPage.getSurvey().getId(),
        surveyPage.getSerialNumber(),
        surveyPage.getTitle(),
        surveyPage.getDescription(),
        surveyPage.getQuestions().stream()
            .map(questionMapper::questionToDto)
            .toList(),
        surveyPage.getConditions().stream()
            .map(ConditionMapper::conditionToDto)
            .toList());
  }

  public SurveyPagePublicResponseDto surveyPageToPublicDto(SurveyPage surveyPage) {
    return new SurveyPagePublicResponseDto(
        surveyPage.getId(),
        surveyPage.getSurvey().getId(),
        surveyPage.getSerialNumber(),
        surveyPage.getTitle(),
        surveyPage.getDescription(),
        surveyPage.getQuestions().stream()
            .map(questionMapper::questionToDto)
            .toList());
  }

  public SurveyPageShortResponseDto surveyPageToShortDto(SurveyPage surveyPage) {
    return new SurveyPageShortResponseDto(
        surveyPage.getId(),
        surveyPage.getSerialNumber());
  }
}
