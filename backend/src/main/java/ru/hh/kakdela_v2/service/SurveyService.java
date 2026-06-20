package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.SurveyNotificationSettingsDao;
import ru.hh.kakdela_v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela_v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyNotificationSettings;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final PermissionService permissionService;
  private final SurveyNotificationSettingsDao notificationSettingsDao;
  private final NotificationService notificationService;

  @Transactional(readOnly = true)
  public SurveyResponseDto getById(UUID id) {
    Survey survey = surveyDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + id));
    return new SurveyResponseDto(survey);
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseDto> getAllByAuthorId(UUID authorId) {
    return surveyDao.findAllByAuthorId(authorId).stream()
            .map(SurveyShortResponseDto::new)
            .toList();
  }

  @Transactional( readOnly = true)
  public List<SurveyShortResponseDto> getMySurveys(UUID accountId) {
    List<Survey> surveys = permissionService.getAccessibleSurveys(accountId);
    return surveys.stream()
        .map(SurveyShortResponseDto::new)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseDto> getAllPublished() {
    return surveyDao.findAllPublished().stream()
            .map(SurveyShortResponseDto::new)
            .toList();
  }

  @Transactional
  public SurveyResponseDto create(UUID authorId, SurveyCreateDto dto) {
    Account author = accountDao.findById(authorId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Аккаунт не найден: " + authorId));

    Survey survey = Survey.builder()
        .author(author)
        .title(dto.getTitle())
        .description(dto.getDescription())
        .isAuthorizedOnly(dto.getIsAuthorizedOnly())
        .isLimitedToOneResponse(dto.getIsLimitedToOneResponse())
        .doNotify(dto.getDoNotify())
        .isPublished(false)
        .isTemplate(false)
        .expireAt(dto.getExpireAt())
        .createdAt(Instant.now())
        .build();

    surveyDao.save(survey);
    saveNotificationSettings(survey.getId(), dto);
    return new SurveyResponseDto(survey);
  }

  @Transactional
  public SurveyResponseDto update(UUID surveyId, SurveyUpdateDto dto, UUID accountId) {
    permissionService.checkAccess(surveyId, accountId, SurveyRole.EDITOR);
    Survey survey = surveyDao.findById(surveyId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));
    
    boolean wasPublished = survey.isPublished();
    
    if (dto.getTitle() != null) survey.setTitle(dto.getTitle());
    if (dto.getDescription() != null) survey.setDescription(dto.getDescription());
    if (dto.getIsAuthorizedOnly() != null) survey.setAuthorizedOnly(dto.getIsAuthorizedOnly());
    if (dto.getIsLimitedToOneResponse() != null) survey.setLimitedToOneResponse(dto.getIsLimitedToOneResponse());
    if (dto.getIsPublished() != null) survey.setPublished(dto.getIsPublished());
    if (dto.getDoNotify() != null) survey.setDoNotify(dto.getDoNotify());
    if (dto.getExpireAt() != null) survey.setExpireAt(dto.getExpireAt());

    if (dto.getIsPublished() && !wasPublished) {
      if (survey.getExpireAt() != null && survey.getExpireAt().isBefore(Instant.now())) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                     "Нельзя опубликовать опрос с истекшим сроком");
                }

          notificationService.sendSurveyPublishedNotifications(surveyId);

    }

    surveyDao.update(survey);
    return new SurveyResponseDto(survey);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    permissionService.checkOwnership(id, accountId);
    Survey survey = surveyDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + id));
    surveyDao.delete(survey);
  }

  private void saveNotificationSettings(UUID surveyId, SurveyCreateDto dto) {
      if (dto.getNotifyEditors() == null && dto.getNotifyAnalysts() == null
                && (dto.getNotifyUserIds() == null || dto.getNotifyUserIds().isEmpty()))
            return;

        SurveyNotificationSettings settings = SurveyNotificationSettings.builder()
                .surveyId(surveyId)
                .notifyEditors(dto.getNotifyEditors() != null && dto.getNotifyEditors())
                .notifyAnalysts(dto.getNotifyAnalysts() != null && dto.getNotifyAnalysts())
                .notifyCustomUserIds(dto.getNotifyUserIds() != null ? dto.getNotifyUserIds() : new ArrayList<>())
                .build();

        notificationSettingsDao.save(settings);
  }

}
