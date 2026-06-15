package ru.hh.kakdela_v2.dto.permission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela_v2.model.Permission;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PermissionUpdateDto {

  private UUID accountId;
  private Permission.SurveyRole role;
  private Boolean doNotify; // nullable — если не передан, не меняем
}
