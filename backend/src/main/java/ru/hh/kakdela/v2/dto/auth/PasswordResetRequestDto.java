package ru.hh.kakdela.v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.Messages;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Auth.PasswordReset.Request")
public class PasswordResetRequestDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  String email;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  String code;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  String newPassword;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  String passwordConfirmation;
}
