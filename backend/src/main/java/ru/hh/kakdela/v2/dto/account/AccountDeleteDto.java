package ru.hh.kakdela.v2.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.Messages;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Account.Delete")
public class AccountDeleteDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = Messages.PASSWORD_CONFIRMATION_NEEDED)
  private String password;
}
