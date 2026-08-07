package ru.hh.kakdela.v2.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.survey.SurveyPublicResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyWithUserRoleDto;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Survey;

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
        survey.doNotify(),
        survey.getExpireAt(),
        survey.getExpireAt() != null
            ? LocalDateTime.ofInstant(survey.getExpireAt(), 
                ZoneId.of(survey.getTargetTimezone()))
            : null,
        survey.getTargetTimezone(),
        survey.getCreatedAt(),
        survey.getPages().stream()
            .map(surveyPageMapper::surveyPageToDto)
            .toList(),
        survey.getClosingPage() != null
            ? closingPageMapper.closingPageToDto(survey.getClosingPage())
            : null
    );
  }

  public SurveyPublicResponseDto surveyToPublicDto(Survey survey) {
    return new SurveyPublicResponseDto(
        survey.getId(),
        AccountMapper.accountToDto(survey.getAuthor()),
        survey.getTitle(),
        survey.getDescription(),
        survey.isAuthorizedOnly(),
        survey.isLimitedToOneResponse(),
        survey.getExpireAt(),
        survey.getExpireAt() != null
            ? LocalDateTime.ofInstant(survey.getExpireAt(),
                ZoneId.of(survey.getTargetTimezone()))
            : null,
        survey.getTargetTimezone(),
        survey.getPages().stream()
            .map(surveyPageMapper::surveyPageToShortDto)
            .toList(),
        survey.getClosingPage() != null);
  }

  public SurveyShortResponseDto surveyToShortDto(Survey survey) {
    return new SurveyShortResponseDto(
        survey.getId(),
        survey.getTitle(),
        survey.getDescription(),
        survey.getCreatedAt(),
        survey.getExpireAt()
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
        dto.getRole());
  }

  public SurveyWithUserRoleDto surveyToSurveyWithRoleDto(
      Survey survey,
      Permission.SurveyRole role
  ) {
    return new SurveyWithUserRoleDto(
        survey,
        role);
  }
}
