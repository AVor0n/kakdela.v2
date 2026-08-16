package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.Permission;

public interface PermissionDao {

  Optional<Permission> findById(Permission.PermissionId id);

  Optional<Permission> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

  List<UUID> findUserIdsBySurveyIdAndRole(UUID surveyId, Permission.SurveyRole role);

  List<Permission> findAllBySurveyId(UUID surveyId);

  List<Permission> findAllWithSurveysBySurveyId(UUID surveyId);

  List<Permission> findAllByAccountId(UUID accountId);

  boolean existsById(Permission.PermissionId id);

  boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

  void save(Permission permission);

  void update(Permission permission);

  void delete(Permission permission);
}
