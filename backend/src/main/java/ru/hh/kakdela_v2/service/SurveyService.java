package ru.hh.kakdela_v2.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela_v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.util.TransactionHelper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

  private final SurveyDao surveyDao;
  private final PermissionService permissionService;
  private final TransactionHelper transactionHelper;
  private final AccountDao accountDao;

  public SurveyResponseDto getById(UUID surveyId, UUID accountId) {
    return transactionHelper.inTransaction(() -> {

      Survey survey = surveyDao.findById(surveyId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

      return new SurveyResponseDto(survey);
    });
  }

  public List<SurveyShortResponseDto> getAllByAuthorId(UUID authorId) {
    return transactionHelper.inTransaction(() ->
            surveyDao.findAllByAuthorId(authorId).stream()
                    .map(SurveyShortResponseDto::new)
                    .toList()
    );
  }

  public List<SurveyShortResponseDto> getMySurveys(UUID accountId) {
    List<Survey> surveys = permissionService.getAccessibleSurveys(accountId);
    return surveys.stream()
        .map(SurveyShortResponseDto::new)
        .collect(Collectors.toList());
  }

  public List<SurveyShortResponseDto> getAllPublished() {
    return transactionHelper.inTransaction(() ->
            surveyDao.findAllPublished().stream()
                    .map(SurveyShortResponseDto::new)
                    .toList()
    );
  }

  public SurveyResponseDto create(UUID authorId, SurveyCreateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Account author = accountDao.findById(authorId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + authorId));

      Survey survey = Survey.builder()
              .author(author)
              .title(dto.getTitle())
              .description(dto.getDescription())
              .isAuthorizedOnly(dto.isAuthorizedOnly())
              .isLimitedToOneResponse(dto.isLimitedToOneResponse())
              .isPublished(false)   // новый опрос всегда черновик
              .isTemplate(false)    // пользователь не создает шаблон
              .doNotify(dto.isDoNotify())
              .expireAt(dto.getExpireAt())
              .build();

      surveyDao.save(survey);
      return new SurveyResponseDto(survey);
    });
  }

  public SurveyResponseDto update(UUID surveyId, SurveyUpdateDto dto, UUID accountId) {
    return transactionHelper.inTransaction(() -> {
      permissionService.checkAccess(surveyId, accountId, SurveyRole.EDITOR);
      Survey survey = surveyDao.findById(surveyId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

      if (dto.getTitle() != null) survey.setTitle(dto.getTitle());
      if (dto.getDescription() != null) survey.setDescription(dto.getDescription());
      if (dto.getAuthorizedOnly() != null) survey.setAuthorizedOnly(dto.getAuthorizedOnly());
      if (dto.getLimitedToOneResponse() != null) survey.setLimitedToOneResponse(dto.getLimitedToOneResponse());
      if (dto.getPublished() != null) survey.setPublished(dto.getPublished());
      if (dto.getDoNotify() != null) survey.setDoNotify(dto.getDoNotify());
      if (dto.getExpireAt() != null) survey.setExpireAt(dto.getExpireAt());

      surveyDao.update(survey);
      return new SurveyResponseDto(survey);
    });
  }

  public void delete(UUID surveyId, UUID accountId) {
    transactionHelper.inTransaction(() -> {
      permissionService.checkOwnership(surveyId, accountId);
      surveyDao.delete(surveyId);
    });
  }
}
