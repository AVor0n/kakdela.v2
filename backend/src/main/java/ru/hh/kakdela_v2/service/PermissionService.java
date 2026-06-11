package ru.hh.kakdela_v2.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import ru.hh.kakdela_v2.dao.PermissionDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dto.permission.PermissionRequestDto;
import ru.hh.kakdela_v2.dto.permission.PermissionResponseDto;
import ru.hh.kakdela_v2.dto.permission.PermissionsListResponseDto;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Permission.PermissionId;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.util.TransactionHelper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PermissionService {

  private final PermissionDao permissionDao;
  private final SurveyDao surveyDao;
  private final AccountDao accountDao;

  @Transactional(readOnly = true)
  public void checkAccess(UUID surveyId, UUID accountId, SurveyRole requiredRole) {
    Survey survey = surveyDao.findById(surveyId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    if (survey.getAuthor().getId().equals(accountId)) {
      return;
    }

    Permission permission = permissionDao.findBySurveyIdAndAccountId(surveyId, accountId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Нет доступа к опросу"));

    if (!hasEnoughRole(permission.getRole(), requiredRole)) {
      throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "Недостаточно прав. Требуется: " + requiredRole);
    }
  }

  @Transactional(readOnly = true)
  public void checkOwnership(UUID surveyId, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    if (!survey.getAuthor().getId().equals(accountId)) {
      throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "Вы не являетесь автором опроса");
    }
  }

  private boolean hasEnoughRole(SurveyRole userRole, SurveyRole requiredRole) {
    if (requiredRole == SurveyRole.EDITOR) {
      return userRole == SurveyRole.EDITOR;
    }
    if (requiredRole == SurveyRole.ANALYST) {
      return userRole == SurveyRole.EDITOR || userRole == SurveyRole.ANALYST;
    }
    return false;
  }

  @Transactional(readOnly = true)
  public List<PermissionResponseDto> getAllBySurveyId(UUID surveyId) {
    return permissionDao.findAllBySurveyId(surveyId).stream()
            .map(PermissionResponseDto::new)
            .toList();
  }

  @Transactional(readOnly = true)
  public List<PermissionResponseDto> getAllByAccountId(UUID accountId) {
    return permissionDao.findAllByAccountId(accountId).stream()
            .map(PermissionResponseDto::new)
            .toList();
  }

  @Transactional
  public PermissionResponseDto create(UUID surveyId, UUID currentUserId, PermissionCreateDto dto) {
    checkOwnership(surveyId, currentUserId);

    if (permissionDao.existsBySurveyIdAndAccountId(surveyId, dto.getAccountId())) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT, "Пользователь уже имеет доступ к этому опросу");
    }

    Account account = accountDao.findById(dto.getAccountId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Аккаунт не найден: " + dto.getAccountId()));

    Survey survey = surveyDao.findById(surveyId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    PermissionId permissionId = PermissionId.builder()
            .accountId(dto.getAccountId())
            .surveyId(surveyId)
            .build();

    Permission permission = Permission.builder()
            .id(permissionId)
            .account(account)
            .survey(survey)
            .role(dto.getRole())
            .doNotify(dto.isDoNotify())
            .build();

    permissionDao.save(permission);
    return new PermissionResponseDto(permission);
  }

  @Transactional
  public PermissionResponseDto update(UUID surveyId, UUID accountId,
                                      UUID currentUserId, PermissionUpdateDto dto) {
    checkOwnership(surveyId, currentUserId);

    Permission permission = permissionDao.findBySurveyIdAndAccountId(surveyId, accountId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Доступ не найден"));

    if (dto.getRole() != null) permission.setRole(dto.getRole());
    if (dto.getDoNotify() != null) permission.setDoNotify(dto.getDoNotify());

    permissionDao.update(permission);
    return new PermissionResponseDto(permission);
  }

  @Transactional
  public void delete(UUID surveyId, UUID accountId, UUID currentUserId) {
    checkOwnership(surveyId, currentUserId);
    permissionDao.deleteBySurveyIdAndAccountId(surveyId, accountId);
  }
  
  @Transactional(readOnly = true)
  public List<Survey> getAccessibleSurveys(UUID accountId) {
    List<Survey> authored = surveyDao.findAllByAuthorId(accountId);
    List<Survey> shared = permissionDao.findAllByAccountId(accountId).stream()
      .map(Permission::getSurvey)
      .toList();

    return Stream.concat(authored.stream(), shared.stream())
      .distinct()
      .collect(Collectors.toList());
  }
}
