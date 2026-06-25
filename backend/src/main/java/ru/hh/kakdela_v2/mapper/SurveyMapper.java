package ru.hh.kakdela_v2.mapper;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hh.kakdela_v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyPage;

@Component
@RequiredArgsConstructor
public class SurveyMapper {

  private final SurveyPageMapper surveyPageMapper;
  private final ClosingPageMapper closingPageMapper;

  public SurveyResponseDto surveyToDto(Survey survey) {
    return new SurveyResponseDto(
        survey.getId(),
        survey.getAuthor().getId(),
        survey.getTitle(),
        survey.getDescription(),
        survey.isAuthorizedOnly(),
        survey.isLimitedToOneResponse(),
        survey.isPublished(),
        survey.isTemplate(),
        survey.isDoNotify(),
        survey.getExpireAt(),
        survey.getCreatedAt(),
        survey.getPages().stream()
            .sorted(Comparator.comparingInt(SurveyPage::getSerialNumber))
            .map(surveyPageMapper::surveyPageToDto)
            .toList(),
        survey.getClosingPage() != null
            ? closingPageMapper.closingPageToDto(survey.getClosingPage())
            : null
    );
  }

  public SurveyShortResponseDto surveyToShortDto(Survey survey) {
    return new SurveyShortResponseDto(
        survey.getId(),
        survey.getTitle(),
        survey.getDescription(),
        survey.isPublished(),
        survey.getCreatedAt()
    );
  }
}
