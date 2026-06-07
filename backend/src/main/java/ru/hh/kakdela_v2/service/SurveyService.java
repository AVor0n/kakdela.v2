package ru.hh.kakdela_v2.service;

import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela_v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.util.TransactionHelper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

public class SurveyService {

  private final SurveyDao surveyDao;
  private final TransactionHelper transactionHelper;
  private final AccountDao accountDao;

  public SurveyService(SurveyDao surveyDao, AccountDao accountDao, TransactionHelper transactionHelper) {
    this.surveyDao = surveyDao;
    this.accountDao = accountDao;
    this.transactionHelper = transactionHelper;
  }

  public SurveyResponseDto getById(UUID id) {
    return transactionHelper.inTransaction(() -> {
      Survey survey = surveyDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + id));
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

  public SurveyResponseDto update(UUID id, SurveyUpdateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Survey survey = surveyDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + id));

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

  public void delete(UUID id) {
    transactionHelper.inTransaction(() -> {
      surveyDao.delete(id);
    });
  }
}
