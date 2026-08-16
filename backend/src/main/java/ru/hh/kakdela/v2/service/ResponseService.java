package ru.hh.kakdela.v2.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.ResponseDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.response.ResponseExportDto;
import ru.hh.kakdela.v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela.v2.dto.response.ResponseWithTokenDto;
import ru.hh.kakdela.v2.mapper.ResponseMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.security.JwtService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseService {

  private final ResponseDao responseDao;
  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final PermissionService permissionService;
  private final JwtService jwtService;
  private final ResponseExportService exportService;

  @Transactional(readOnly = true)
  public ResponseResponseDto getById(UUID id, UUID accountId, String token) {
    Response response = loadResponseAndCheckAccess(id, accountId, token);

    if (response.getSurvey().isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
    }

    if (response.getAccount() == null && response.isCompleted()
        && !response.getSurvey().isAuthor(accountId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Просмотр завершённых анонимных ответов запрещён");
    }

    return ResponseMapper.responseToDto(response);
  }

  @Transactional(readOnly = true)
  public List<ResponseResponseDto> getCompletedBySurveyId(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId));

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
    }

    permissionService.checkCanReadResponses(surveyId, accountId);

    return responseDao.findCompletedBySurveyId(surveyId).stream()
        .map(ResponseMapper::responseToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ResponseResponseDto> getAllByAccountId(UUID accountId) {
    return responseDao.findAllByAccountId(accountId).stream()
        .filter(response -> !response.getSurvey().isTemplate())
        .map(ResponseMapper::responseToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ResponseResponseDto> getIncompletedBySurveyIdAndAccountId(
      UUID surveyId,
      UUID accountId
  ) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId));

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
    }

    return responseDao.findIncompletedBySurveyIdAndAccountId(surveyId, accountId).stream()
        .map(ResponseMapper::responseToDto)
        .toList();
  }

  @Transactional
  public ResponseWithTokenDto create(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
    }

    if (!survey.isPublished()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Опрос ещё не опубликован");
    }

    if (survey.getExpireAt() != null && survey.getExpireAt().isBefore(Instant.now())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Дедлайн прохождения опроса истёк");
    }

    if (survey.isAuthorizedOnly() && accountId == null) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Опрос доступен только авторизованным пользователям");
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
        .id(UUID.randomUUID())
        .account(account)
        .survey(survey)
        .build();

    responseDao.save(response);
    log.info("Создан ответ на опрос id={} surveyId={} accountId={}",
        response.getId(), surveyId, accountId);

    return new ResponseWithTokenDto(
        response.getId(),
        accountId != null
            ? null
            : jwtService.generateResponseAccessToken(response.getId()));
  }

  @Transactional
  public ResponseResponseDto complete(UUID id, UUID accountId, String token) {
    Response response = loadResponseAndCheckAccess(id, accountId, token);

    if (response.getSurvey().isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
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
    response.setReceivedAt(Instant.now());

    responseDao.update(response);
    log.info("Завершен ответ на опрос id={} accountId={}", id, accountId);

    return ResponseMapper.responseToDto(response);
  }

  @Transactional
  public ResponseExportDto export(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId));

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Опрос не найден"
      );
    }

    permissionService.checkCanReadResponses(surveyId, accountId);

    List<ResponseResponseDto> completedResponses = responseDao
        .findCompletedBySurveyId(surveyId).stream()
        .map(ResponseMapper::responseToDto)
        .toList();

    ResponseExportDto excelData;
    try {
      excelData = exportService.exportResponsesWithFilename(
          completedResponses,
          surveyId
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return excelData;
  }

  // Вспомогательные методы

  Response loadResponseAndCheckAccess(UUID responseId, UUID accountId, String token) {
    Response response = responseDao.findById(responseId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Ответ не найден: " + responseId));

    if (response.isCompleted() && response.getSurvey().isAuthor(accountId)) {
      return response;
    }

    if (response.getAccount() == null && token == null) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Не предоставлены учётные данные для доступа к прохождению");
    }

    if (response.getAccount() != null && !response.getAccount().getId().equals(accountId)
        || token != null && !Objects.equals(jwtService.extractResponseId(token), responseId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Вы не являетесь автором ответа");
    }

    return response;
  }
}
