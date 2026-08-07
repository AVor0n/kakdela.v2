package ru.hh.kakdela.v2.service;

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
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
import ru.hh.kakdela.v2.dto.permission.PermissionCreateDto;
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
  public void checkHasAnyPermission(UUID surveyId, UUID accountId) {
    Survey survey = getSurveyOrThrow(surveyId);

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Нельзя управлять правами доступа для шаблона"
      );
    }

    if (survey.getAuthor().getId().equals(accountId)) {
      return;
    }

    getPermissionOrThrow(surveyId, accountId);
  }

  @Transactional(readOnly = true)
  public void checkCanReadResponses(UUID surveyId, UUID accountId) {
    Survey survey = getSurveyOrThrow(surveyId);

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Нельзя управлять правами доступа для шаблона"
      );
    }

    if (survey.getAuthor().getId().equals(accountId)) {
      return;
    }

    Permission permission = getPermissionOrThrow(surveyId, accountId);

    if (!permission.getRole().isResponseReadAccess()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "У вас нет прав на просмотр ответов");
    }
  }

  @Transactional(readOnly = true)
  public void checkCanEdit(UUID surveyId, UUID accountId) {
    Survey survey = getSurveyOrThrow(surveyId);

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Нельзя управлять правами доступа для шаблона"
      );
    }

    if (survey.getAuthor().getId().equals(accountId)) {
      return;
    }

    Permission permission = getPermissionOrThrow(surveyId, accountId);

    if (!permission.getRole().isEditAccess()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "У вас нет прав на редактирование опроса");
    }
  }

  @Transactional(readOnly = true)
  public void checkCanDelete(UUID surveyId, UUID accountId) {
    Survey survey = getSurveyOrThrow(surveyId);

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Нельзя управлять правами доступа для шаблона"
      );
    }

    if (survey.getAuthor().getId().equals(accountId)) {
      return;
    }

    Permission permission = getPermissionOrThrow(surveyId, accountId);

    if (!permission.getRole().isSurveyDeleteAccess()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "У вас нет прав на удаление опроса");
    }
  }

  @Transactional(readOnly = true)
  public List<PermissionResponseDto> getAllBySurveyId(UUID surveyId) {
    Survey survey = getSurveyOrThrow(surveyId);

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Нельзя управлять правами доступа для шаблона"
      );
    }
    return permissionDao.findAllBySurveyId(surveyId).stream()
        .map(PermissionMapper::permissionToDto)
        .toList();
  }

  @Transactional
  public PermissionResponseDto create(
      UUID surveyId,
      PermissionCreateDto dto,
      UUID currentUserId
  ) {
    Survey survey = getSurveyOrThrow(surveyId);

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Нельзя управлять правами доступа для шаблона"
      );
    }

    checkCanManagePermissions(survey, currentUserId);

    Account account = accountDao.findByEmail(dto.getEmail())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден: email=" + dto.getEmail()));

    if (permissionDao.existsBySurveyIdAndAccountId(surveyId, account.getId())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Пользователь уже имеет права доступа к этому опросу");
    }

    PermissionId permissionId = PermissionId.builder()
        .surveyId(surveyId)
        .accountId(account.getId())
        .build();

    Permission permission = Permission.builder()
        .id(permissionId)
        .survey(survey)
        .account(account)
        .role(dto.getRole())
        .build();

    permissionDao.save(permission);
    log.info("Созданы права доступа: surveyId={}, accountId={}, role={}",
        surveyId, account.getId(), dto.getRole());
    return PermissionMapper.permissionToDto(permission);
  }

  @Transactional
  public PermissionResponseDto updateFull(
      UUID surveyId,
      UUID accountId,
      PermissionUpdateDto dto,
      UUID currentUserId
  ) {
    Survey survey = getSurveyOrThrow(surveyId);

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Нельзя управлять правами доступа для шаблона"
      );
    }

    checkCanManagePermissions(survey, currentUserId);

    Permission permission = permissionDao.findBySurveyIdAndAccountId(surveyId, accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Права доступа не найдены: surveyId=%s, accountId=%s"
            .formatted(surveyId, accountId)));

    permission.setRole(dto.getRole());

    permissionDao.update(permission);
    log.info("Заменены права доступа: surveyId={}, accountId={}", surveyId, accountId);

    return PermissionMapper.permissionToDto(permission);
  }

  @Transactional
  public void delete(UUID surveyId, UUID accountId, UUID currentUserId) {
    Survey survey = getSurveyOrThrow(surveyId);

    if (survey.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Нельзя управлять правами доступа для шаблона"
      );
    }

    checkCanManagePermissions(survey, currentUserId);

    permissionDao.deleteBySurveyIdAndAccountId(surveyId, accountId);
    log.info("Удалены права доступа: surveyId={}, accountId={}", surveyId, accountId);
  }

  @Transactional(readOnly = true)
  public List<SurveyWithUserRoleDto> getAccessibleSurveys(UUID accountId) {
    List<SurveyWithUserRoleDto> authored = surveyDao.findAllByAuthorId(accountId).stream()
        .filter(survey -> !survey.isTemplate())
        .map(survey ->
            surveyMapper.surveyToSurveyWithRoleDto(survey, SurveyRole.AUTHOR)
        )
        .toList();

    List<SurveyWithUserRoleDto> shared = permissionDao.findAllByAccountId(accountId).stream()
        .filter(permission -> !permission.getSurvey().isTemplate())
        .map(permission ->
            surveyMapper.surveyToSurveyWithRoleDto(permission.getSurvey(), permission.getRole())
        )
        .toList();

    return Stream.concat(authored.stream(), shared.stream())
        .distinct()
        .sorted(
            Comparator.comparing(
                (SurveyWithUserRoleDto dto) -> dto.getSurvey().getCreatedAt()
            ).reversed()
        )
        .collect(toList());
  }

  // Вспомогательные методы

  private Survey getSurveyOrThrow(UUID surveyId) {
    return surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId));
  }

  private Permission getPermissionOrThrow(UUID surveyId, UUID accountId) {
    return permissionDao.findBySurveyIdAndAccountId(surveyId, accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.FORBIDDEN, "У вас нет прав доступа к опросу"));
  }

  private void checkCanManagePermissions(Survey survey, UUID accountId) {
    if (survey.getAuthor().getId().equals(accountId)) {
      return;
    }

    Permission permission = getPermissionOrThrow(survey.getId(), accountId);

    if (!permission.getRole().isPermissionManagementAccess()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "У вас нет прав на управление доступом к опросу");
    }
  }
}
