package ru.hh.kakdela_v2.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.PermissionDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dto.permission.PermissionCreateDto;
import ru.hh.kakdela_v2.dto.permission.PermissionResponseDto;
import ru.hh.kakdela_v2.dto.permission.PermissionUpdateDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.util.TransactionHelper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PermissionService {

  private final PermissionDao permissionDao;
  private final AccountDao accountDao;
  private final SurveyDao surveyDao;
  private final TransactionHelper transactionHelper;

  public PermissionService(PermissionDao permissionDao, AccountDao accountDao,
                           SurveyDao surveyDao, TransactionHelper transactionHelper) {
    this.permissionDao = permissionDao;
    this.accountDao = accountDao;
    this.surveyDao = surveyDao;
    this.transactionHelper = transactionHelper;
  }

  public List<PermissionResponseDto> getAllBySurveyId(UUID surveyId) {
    return transactionHelper.inTransaction(() ->
            permissionDao.findAllBySurveyId(surveyId).stream()
                    .map(PermissionResponseDto::new)
                    .toList()
    );
  }

  public PermissionResponseDto create(UUID surveyId, PermissionCreateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Permission.PermissionId permissionId = Permission.PermissionId.builder()
              .accountId(dto.getAccountId())
              .surveyId(surveyId)
              .build();

      // проверка — такой доступ уже есть
      if (permissionDao.existsById(permissionId)) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Пользователь уже имеет доступ к этому опросу");
      }

      Account account = accountDao.findById(dto.getAccountId())
              .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND, "Аккаунт не найден: " + dto.getAccountId()));

      Survey survey = surveyDao.findById(surveyId)
              .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

      Permission permission = Permission.builder()
              .id(permissionId)
              .account(account)
              .survey(survey)
              .role(parseRole(dto.getRole()))
              .doNotify(dto.isDoNotify())
              .build();

      permissionDao.save(permission);
      return new PermissionResponseDto(permission);
    });
  }

  public PermissionResponseDto update(UUID surveyId, UUID accountId, PermissionUpdateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Permission.PermissionId permissionId = Permission.PermissionId.builder()
              .accountId(accountId)
              .surveyId(surveyId)
              .build();

      Permission permission = permissionDao.findById(permissionId)
              .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND, "Доступ не найден"));

      if (dto.getRole() != null) permission.setRole(parseRole(dto.getRole()));
      if (dto.getDoNotify() != null) permission.setDoNotify(dto.getDoNotify());

      permissionDao.update(permission);
      return new PermissionResponseDto(permission);
    });
  }

  public void delete(UUID surveyId, UUID accountId) {
    transactionHelper.inTransaction(() -> {
      Permission.PermissionId id = Permission.PermissionId.builder()
              .accountId(accountId)
              .surveyId(surveyId)
              .build();
      permissionDao.delete(id);
    });
  }

  private Permission.SurveyRole parseRole(String role) {
    try {
      return Permission.SurveyRole.valueOf(role);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Недопустимая роль: " + role + ". Допустимые значения: "
                      + Arrays.toString(Permission.SurveyRole.values())
      );
    }
  }
}
