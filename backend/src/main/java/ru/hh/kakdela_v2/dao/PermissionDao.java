package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionDao {

  Optional<Permission> findById(Permission.PermissionId id);

  List<Permission> findAllBySurveyId(UUID surveyId);

  List<Permission> findAllByAccountId(UUID accountId);

  boolean existsById(Permission.PermissionId id);

  void save(Permission permission);

  void update(Permission permission);

  void delete(Permission.PermissionId id);
}
