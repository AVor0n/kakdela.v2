package ru.hh.kakdela.v2.mapper;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageResponseDto;
import ru.hh.kakdela.v2.model.Question;
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
            .sorted(Comparator.comparingInt(Question::getSerialNumber))
            .map(questionMapper::questionToDto)
            .toList()
    );
  }
}
