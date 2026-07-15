package ru.hh.kakdela.v2.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.PermissionDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.permission.PermissionRequestDto;
import ru.hh.kakdela.v2.dto.permission.PermissionResponseDto;
import ru.hh.kakdela.v2.dto.permission.PermissionUpdateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyWithUserRoleDto;
import ru.hh.kakdela.v2.mapper.PermissionMapper;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Permission.PermissionId;
import ru.hh.kakdela.v2.model.Permission.SurveyRole;
import ru.hh.kakdela.v2.model.Survey;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

  private final PermissionDao permissionDao;
  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final SurveyMapper surveyMapper;

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
        .map(PermissionMapper::permissionToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PermissionResponseDto> getAllByAccountId(UUID accountId) {
    return permissionDao.findAllByAccountId(accountId).stream()
        .map(PermissionMapper::permissionToDto)
        .toList();
  }

  @Transactional
  public PermissionResponseDto create(UUID surveyId, UUID currentUserId, PermissionRequestDto dto) {
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
        .doNotify(dto.getDoNotify())
        .build();

    permissionDao.save(permission);
    log.info("Созданы права доступа surveyId={} accountId={} role={}",
        surveyId, dto.getAccountId(), dto.getRole());
    return PermissionMapper.permissionToDto(permission);
  }

  @Transactional
  public PermissionResponseDto updateFull(
      UUID surveyId,
      UUID accountId,
      UUID currentUserId,
      PermissionRequestDto dto
  ) {
    checkOwnership(surveyId, currentUserId);

    Permission permission = permissionDao.findBySurveyIdAndAccountId(surveyId, accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Доступ не найден"));

    permission.setRole(dto.getRole());
    permission.setDoNotify(dto.getDoNotify());

    permissionDao.update(permission);
    log.info("Изменены права доступа (full update) surveyId={} accountId={}", surveyId, accountId);

    return PermissionMapper.permissionToDto(permission);
  }

  @Transactional
  public PermissionResponseDto updatePartial(
      UUID surveyId,
      UUID accountId,
      UUID currentUserId,
      PermissionUpdateDto dto
  ) {
    checkOwnership(surveyId, currentUserId);

    Permission permission = permissionDao.findBySurveyIdAndAccountId(surveyId, accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Доступ не найден"));

    if (dto.getRole() != null) {
      permission.setRole(dto.getRole());
    }
    if (dto.getDoNotify() != null) {
      permission.setDoNotify(dto.getDoNotify());
    }

    permissionDao.update(permission);
    log.info("Изменены права доступа (partial update) surveyId={} accountId={}",
        surveyId, accountId);
    return PermissionMapper.permissionToDto(permission);
  }

  @Transactional
  public void delete(UUID surveyId, UUID accountId, UUID currentUserId) {
    checkOwnership(surveyId, currentUserId);
    permissionDao.deleteBySurveyIdAndAccountId(surveyId, accountId);
    log.info("Удалены права доступа surveyId={} accountId={}", surveyId, accountId);
  }

  @Transactional(readOnly = true)
  public List<SurveyWithUserRoleDto> getAccessibleSurveys(UUID accountId) {
    List<SurveyWithUserRoleDto> authored = surveyDao.findAllByAuthorId(accountId).stream()
        .map(survey ->
            surveyMapper.surveyToRoleDto(survey, SurveyRole.AUTHOR)
        )
        .toList();

    List<SurveyWithUserRoleDto> shared = permissionDao.findAllByAccountId(accountId).stream()
        .map(permission ->
            surveyMapper.surveyToRoleDto(permission.getSurvey(), permission.getRole())
        )
        .toList();

    return Stream.concat(authored.stream(), shared.stream())
        .distinct()
        .collect(toList());
  }
}
