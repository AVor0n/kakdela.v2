package ru.hh.kakdela.v2.dto.permission;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.model.Permission;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "PermissionCreate",
    title = "DTO для создания роли"
)
public class PermissionRequestDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "ID аккаунта обязателен")
  private UUID accountId;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Роль обязательна")
  private Permission.SurveyRole role;
  @Schema(description = "Это необязательное поле, у него есть дефолтное значение")
  @NotNull
  private Boolean doNotify = true;
}
