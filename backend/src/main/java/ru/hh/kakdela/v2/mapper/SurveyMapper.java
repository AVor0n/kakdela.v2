package ru.hh.kakdela.v2.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.survey.SurveyPublicResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseWithPermissionDto;
import ru.hh.kakdela.v2.dto.survey.SurveyWithUserRoleDto;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.service.ObjectStorageService;

@Component
@RequiredArgsConstructor
public class SurveyMapper {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final SurveyPageMapper surveyPageMapper;
  private final ClosingPageMapper closingPageMapper;
  private final ObjectStorageService objectStorageService;

  public SurveyResponseDto surveyToDto(Survey survey) {
    String attachmentUrl = survey.getAttachmentObjectKey() != null
        ? objectStorageService.generateObjectUrl(
        survey.getAttachmentObjectKey(),
        attachmentUrlMaxAge).toString()
        : null;

    return new SurveyResponseDto(
        survey.getId(),
        AccountMapper.accountToDto(survey.getAuthor()),
        survey.getTitle(),
        survey.getDescription(),
        attachmentUrl,
        survey.isAuthorizedOnly(),
        survey.isLimitedToOneResponse(),
        survey.isPublished(),
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

  public SurveyPublicResponseDto surveyToPublicDto(Survey survey, boolean hasConditions) {
    String attachmentUrl = survey.getAttachmentObjectKey() != null
        ? objectStorageService.generateObjectUrl(
        survey.getAttachmentObjectKey(),
        attachmentUrlMaxAge).toString()
        : null;

    return new SurveyPublicResponseDto(
        survey.getId(),
        AccountMapper.accountToDto(survey.getAuthor()),
        survey.getTitle(),
        survey.getDescription(),
        attachmentUrl,
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
        survey.getClosingPage() != null,
        hasConditions);
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
