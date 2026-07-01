package ru.hh.kakdela.v2.mapper;

import ru.hh.kakdela.v2.dto.permission.PermissionResponseDto;
import ru.hh.kakdela.v2.model.Permission;

public class PermissionMapper {

  public static PermissionResponseDto permissionToDto(Permission permission) {
    return new PermissionResponseDto(
        permission.getId().getAccountId(),
        permission.getId().getSurveyId(),
        permission.getRole().name(),
        permission.isDoNotify()
    );
  }
}
