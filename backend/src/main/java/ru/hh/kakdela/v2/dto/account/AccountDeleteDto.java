package ru.hh.kakdela.v2.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.ConstraintMessages;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Account.Delete")
public class AccountDeleteDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = ConstraintMessages.PASSWORD_CONFIRMATION_NEEDED)
  private String password;
}
