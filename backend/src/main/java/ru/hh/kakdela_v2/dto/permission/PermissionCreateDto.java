package ru.hh.kakdela_v2.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import ru.hh.kakdela_v2.model.Permission;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCreateDto {

  @NotNull(message = "ID аккаунта обязателен")
  private UUID accountId;

  @NotBlank(message = "Роль обязательна")
  private Permission.SurveyRole role;

  private boolean doNotify;
}
