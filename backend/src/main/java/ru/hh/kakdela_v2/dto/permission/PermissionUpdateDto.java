package ru.hh.kakdela_v2.dto.permission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ru.hh.kakdela_v2.model.Permission;

@Getter
@Setter
@NoArgsConstructor
public class PermissionUpdateDto {

  private Permission.SurveyRole role;
  private Boolean doNotify;   // nullable — если не передан, не меняем
}
