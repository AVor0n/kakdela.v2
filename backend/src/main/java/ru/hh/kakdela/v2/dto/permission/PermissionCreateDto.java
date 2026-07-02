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
public class PermissionCreateDto {

@Schema(
        requiredMode = Schema.RequiredMode.REQUIRED
    )
  @NotNull(message = "ID аккаунта обязателен")
  private UUID accountId;
  @Schema(
        requiredMode = Schema.RequiredMode.REQUIRED
    )
  @NotNull(message = "Роль обязательна")
  private Permission.SurveyRole role;
  @NotNull
  private Boolean doNotify = true;
}
