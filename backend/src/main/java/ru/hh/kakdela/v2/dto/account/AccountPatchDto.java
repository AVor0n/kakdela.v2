package ru.hh.kakdela.v2.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;
import ru.hh.kakdela.v2.constants.TextValueLengthLimits;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;
import ru.hh.kakdela.v2.validator.ValidPassword;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Account.Patch")
public class AccountPatchDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  @Size(max = TextValueLengthLimits.LOGIN_MAX_LENGTH,
      message = ConstraintMessages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED
          + TextValueLengthLimits.LOGIN_MAX_LENGTH)
  private String login;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  @Email(message = ConstraintMessages.FIELD_FORMAT_VIOLATED)
  private String email;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = ConstraintMessages.FIELD_SHOULD_NOT_BE_EMPTY)
  @ValidPassword
  private String newPassword;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String newPasswordConfirmation;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = ConstraintMessages.PASSWORD_CONFIRMATION_NEEDED)
  private String password;
}
