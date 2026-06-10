package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionDao {

  Optional<Permission> findById(Permission.PermissionId id);

  Optional<Permission> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

  List<Permission> findAllBySurveyId(UUID surveyId);

  List<Permission> findAllByAccountId(UUID accountId);

  boolean existsById(Permission.PermissionId id);

  boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

  void save(Permission permission);

  void update(Permission permission);

  void delete(Permission.PermissionId id);

  void deleteBySurveyIdAndAccountId(UUID surveyId, UUID accountId);
}
