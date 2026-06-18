package ru.hh.kakdela_v2.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.model.Permission;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PermissionResponseDto {

  private final UUID accountId;
  private final UUID surveyId;
  private final String role;
  private final Boolean doNotify;
}
