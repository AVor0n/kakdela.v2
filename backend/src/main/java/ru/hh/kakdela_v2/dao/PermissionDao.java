package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Permission.PermissionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionDao {

    Optional<Permission> findById(PermissionId id);

    Optional<Permission> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

    List<Permission> findAllBySurveyId(UUID surveyId);

    List<Permission> findAllByAccountId(UUID accountId);

    List<Permission> findAll();

    void save(Permission permission);

    void update(Permission permission);

    void delete(PermissionId id);

    void deleteBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

    boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId);
}
