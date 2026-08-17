package ru.hh.kakdela.v2.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;
import ru.hh.kakdela.v2.model.Permission;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Permission.Create")
public class PermissionCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  @Email(message = ConstraintMessages.FIELD_FORMAT_VIOLATED)
  private String email;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  private Permission.SurveyRole role;
}
