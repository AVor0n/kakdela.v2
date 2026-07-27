package ru.hh.kakdela.v2.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.model.Permission;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "Permission.Update"
)
public class PermissionUpdateDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Роль обязательна")
  private Permission.SurveyRole role;
}
