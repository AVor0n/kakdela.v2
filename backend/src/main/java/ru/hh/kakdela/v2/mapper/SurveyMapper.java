package ru.hh.kakdela.v2.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyWithUserRoleDto;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

@Component
@RequiredArgsConstructor
public class SurveyMapper {

  private final SurveyPageMapper surveyPageMapper;
  private final ClosingPageMapper closingPageMapper;

  public SurveyResponseDto surveyToDto(Survey survey) {
    return new SurveyResponseDto(
        survey.getId(),
        AccountMapper.accountToDto(survey.getAuthor()),
        survey.getTitle(),
        survey.getDescription(),
        survey.isAuthorizedOnly(),
        survey.isLimitedToOneResponse(),
        survey.isPublished(),
        survey.isTemplate(),
        survey.isDoNotify(),
        survey.getExpireAt(),
        survey.getExpireAt() != null
            ? LocalDateTime.ofInstant(survey.getExpireAt(), 
                ZoneId.of(survey.getTargetTimezone()))
            : null,
        survey.getTargetTimezone(),
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

  public SurveyShortResponseWithPermissionDto surveyToShortDto(
      Survey survey,
      Permission.SurveyRole role
  ) {
    return new SurveyShortResponseWithPermissionDto(
        survey.getId(),
        survey.getTitle(),
        survey.getDescription(),
        survey.isPublished(),
        survey.getCreatedAt(),
        role
    );
  }

  public SurveyShortResponseWithPermissionDto surveyWithRoleDtoToShortDto(
      SurveyWithUserRoleDto dto
  ) {
    return new SurveyShortResponseWithPermissionDto(
        dto.getSurvey().getId(),
        dto.getSurvey().getTitle(),
        dto.getSurvey().getDescription(),
        dto.getSurvey().isPublished(),
        dto.getSurvey().getCreatedAt(),
        dto.getRole()
    );
  }

  public SurveyWithUserRoleDto surveyToSurveyWithRoleDto(
      Survey survey,
      Permission.SurveyRole role
  ) {
    return new SurveyWithUserRoleDto(
        survey,
        role
    );
  }
}
