package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.ResponseDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dto.response.ResponseCreateResponseDto;
import ru.hh.kakdela_v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela_v2.dto.response.ResponseWithTokenDto;
import ru.hh.kakdela_v2.model.*;
import ru.hh.kakdela_v2.util.JwtUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResponseService {

  private final ResponseDao responseDao;
  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final PermissionService permissionService;
  private final JwtUtil jwtUtil;

  @Transactional(readOnly = true)
  public ResponseResponseDto getById(UUID id, UUID accountId, String token) {
    Response response = responseDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ответ не найден: " + id));

    if (!Objects.equals(response.getAccount().getId(), accountId)
        || !Objects.equals(jwtUtil.extractSubject(token), id.toString())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Вы не являетесь автором ответа");
    }

    if (response.getAccount() == null && response.isCompleted()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Просмотр завершённых анонимных ответов запрещён");
    }

    return new ResponseResponseDto(response);
  }

  @Transactional(readOnly = true)
  public List<ResponseResponseDto> getCompletedBySurveyId(UUID surveyId, UUID accountId) {
    permissionService.checkAccess(surveyId, accountId, Permission.SurveyRole.ANALYST);
    return responseDao.findCompletedBySurveyId(surveyId).stream()
            .map(ResponseResponseDto::new)
            .toList();
  }

  @Transactional(readOnly = true)
  public List<ResponseResponseDto> getAllByAccountId(UUID accountId) {
    return responseDao.findAllByAccountId(accountId).stream()
        .map(ResponseResponseDto::new)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ResponseResponseDto> getIncompletedBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
    return responseDao.findIncompletedBySurveyIdAndAccountId(surveyId, accountId).stream()
        .map(ResponseResponseDto::new)
        .toList();
  }

  @Transactional
  public ResponseWithTokenDto create(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    if (!survey.isPublished()) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Опрос ещё не опубликован");
    }

    if (survey.isLimitedToOneResponse() && accountId != null) {
      if (responseDao.existsBySurveyIdAndAccountId(surveyId, accountId)) {
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
            .account(account)
            .survey(survey)
            .isCompleted(false)
            .build();

    responseDao.save(response);

    if (accountId == null) {
      return  new ResponseWithTokenDto(response.getId(),
          jwtUtil.generateResponseEditToken(response.getId()));
    }

    return new ResponseWithTokenDto(response.getId(), null);
  }

  @Transactional
  public ResponseResponseDto complete(UUID id, UUID accountId, String token) {
    Response response = responseDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ответ не найден: " + id));

    if (!Objects.equals(response.getAccount().getId(), accountId)
        || !Objects.equals(jwtUtil.extractSubject(token), id.toString())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Вы не являетесь автором ответа");
    }

    if (!responseDao.areAllMandatoryQuestionsAnswered(id)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Не все обязательные вопросы заполнены");
    }

    if (response.isCompleted()) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT, "Прохождение уже завершено");
    }

    response.setCompleted(true);
    responseDao.update(response);
    return new ResponseResponseDto(response);
  }

  @Transactional
  public void delete(UUID id) {
    Response response = responseDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ответ не найден: " + id));
    responseDao.delete(response);
  }
}
