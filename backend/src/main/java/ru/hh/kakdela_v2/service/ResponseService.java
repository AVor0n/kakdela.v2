package ru.hh.kakdela_v2.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.ResponseDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dto.response.ResponseCreateDto;
import ru.hh.kakdela_v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Response;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.util.TransactionHelper;

import java.util.List;
import java.util.UUID;

public class ResponseService {

  private final ResponseDao responseDao;
  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final TransactionHelper transactionHelper;

  public ResponseService(ResponseDao responseDao, SurveyDao surveyDao,
                         AccountDao accountDao, TransactionHelper transactionHelper) {
    this.responseDao = responseDao;
    this.surveyDao = surveyDao;
    this.accountDao = accountDao;
    this.transactionHelper = transactionHelper;
  }

  public ResponseResponseDto getById(UUID id) {
    return transactionHelper.inTransaction(() -> {
      Response response = responseDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND, "Ответ не найден: " + id));
      return new ResponseResponseDto(response);
    });
  }

  public List<ResponseResponseDto> getAllBySurveyId(UUID surveyId) {
    return transactionHelper.inTransaction(() ->
            responseDao.findAllBySurveyId(surveyId).stream()
                    .map(ResponseResponseDto::new)
                    .toList()
    );
  }

  public ResponseResponseDto create(UUID accountId, ResponseCreateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Survey survey = surveyDao.findById(dto.getSurveyId())
              .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND, "Опрос не найден: " + dto.getSurveyId()));

      // проверка — опрос опубликован
      if (!survey.isPublished()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Опрос ещё не опубликован");
      }

      // проверка — один ответ если включено ограничение
      if (survey.isLimitedToOneResponse() && accountId != null) {
        if (responseDao.existsByAccountIdAndSurveyId(accountId, dto.getSurveyId())) {
          throw new ResponseStatusException(
                  HttpStatus.CONFLICT, "Вы уже проходили этот опрос");
        }
      }

      Account account = null;
      if (accountId != null) {
        account = accountDao.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Аккаунт не найден: " + accountId));
      }

      Response response = Response.builder()
              .account(account)   // null для анонимных
              .survey(survey)
              .isComplete(false)  // новый ответ всегда незавершён
              .build();

      responseDao.save(response);
      return new ResponseResponseDto(response);
    });
  }

  // завершить прохождение опроса
  public ResponseResponseDto complete(UUID id) {
    return transactionHelper.inTransaction(() -> {
      Response response = responseDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND, "Ответ не найден: " + id));

      if (response.isComplete()) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Прохождение уже завершено");
      }

      response.setComplete(true);
      responseDao.update(response);
      return new ResponseResponseDto(response);
    });
  }

  public void delete(UUID id) {
    transactionHelper.inTransaction(() -> responseDao.delete(id));
  }
}
