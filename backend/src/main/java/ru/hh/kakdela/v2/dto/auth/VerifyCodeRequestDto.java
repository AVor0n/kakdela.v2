package ru.hh.kakdela.v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Auth.VerifyCode.Request")
public class VerifyCodeRequestDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  String email;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  String code;
}
