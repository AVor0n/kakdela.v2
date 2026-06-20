package ru.hh.kakdela_v2.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.ResponseDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela_v2.dto.response.ResponseWithTokenDto;
import ru.hh.kakdela_v2.mapper.ResponseMapper;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Response;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class ResponseService {

  private final ResponseDao responseDao;
  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final PermissionService permissionService;
  private final JwtUtil jwtUtil;

  private Response checkAccessAndGetResponse(UUID responseId, UUID accountId, String token) {
    Response response = responseDao.findById(responseId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Ответ не найден: " + responseId));

    if (response.getAccount() == null && token == null) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Не предоставлены учётные данные для доступа к прохождению");
    }

    if (response.getAccount() != null && !response.getAccount().getId().equals(accountId)
        || token != null && !Objects.equals(jwtUtil.extractSubject(token), responseId.toString())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Вы не являетесь автором ответа");
    }

    return response;
  }

  @Transactional(readOnly = true)
  public ResponseResponseDto getById(UUID id, UUID accountId, String token) {
    Response response = checkAccessAndGetResponse(id, accountId, token);

    if (response.getAccount() == null && response.isCompleted()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Просмотр завершённых анонимных ответов запрещён");
    }

    return ResponseMapper.responseToDto(response);
  }

  @Transactional(readOnly = true)
  public List<ResponseResponseDto> getCompletedBySurveyId(UUID surveyId, UUID accountId) {
    permissionService.checkAccess(surveyId, accountId, Permission.SurveyRole.ANALYST);
    return responseDao.findCompletedBySurveyId(surveyId).stream()
        .map(ResponseMapper::responseToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ResponseResponseDto> getAllByAccountId(UUID accountId) {
    return responseDao.findAllByAccountId(accountId).stream()
        .map(ResponseMapper::responseToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ResponseResponseDto> getIncompletedBySurveyIdAndAccountId(UUID surveyId,
                                                                        UUID accountId) {
    return responseDao.findIncompletedBySurveyIdAndAccountId(surveyId, accountId).stream()
        .map(ResponseMapper::responseToDto)
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
      return new ResponseWithTokenDto(response.getId(),
          jwtUtil.generateResponseEditToken(response.getId()));
    }

    return new ResponseWithTokenDto(response.getId(), null);
  }

  @Transactional
  public ResponseResponseDto complete(UUID id, UUID accountId, String token) {
    Response response = checkAccessAndGetResponse(id, accountId, token);

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

    return ResponseMapper.responseToDto(response);
  }

  @Transactional
  public void delete(UUID id) {
    Response response = responseDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Ответ не найден: " + id));
    responseDao.delete(response);
  }
}
