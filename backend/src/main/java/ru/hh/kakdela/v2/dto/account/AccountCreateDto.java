package ru.hh.kakdela.v2.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.validator.ValidPassword;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "AccountCreate",
    title = "DTO для создания аккаунта"
)
public class AccountCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Логин не должен быть пустым")
  @Size(max = 32, message = "Логин не должен быть длиннее 32 символов")
  private String login;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Электронная почта не должна быть пустой")
  @Email(message = "Электронная почта должна соответствовать формату")
  private String email;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Пароль не должен быть пустым")
  @ValidPassword
  private String password;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String passwordConfirmation;
}
