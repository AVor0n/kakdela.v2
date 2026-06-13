package ru.hh.kakdela_v2.dto.permission;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela_v2.model.Permission;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PermissionCreateDto {

  @NotNull(message = "ID аккаунта обязателен")
  private UUID accountId;
  @NotNull(message = "Роль обязательна")
  private Permission.SurveyRole role;
  private Boolean doNotify;
}
