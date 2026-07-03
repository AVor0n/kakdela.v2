package ru.hh.kakdela.v2.dto.account;

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
public class AccountPutDto {

  @NotBlank(message = "Логин не должен быть пустым")
  @Size(max = 32, message = "Логин не должен быть длиннее 32 символов")
  private String login;
  @NotBlank(message = "Электронная почта не должна быть пустой")
  @Email(message = "Электронная почта должна соответствовать формату")
  private String email;
  @NotBlank(message = "Пароль не должен быть пустым")
  @ValidPassword
  private String newPassword;
  private String newPasswordConfirmation;
  @NotBlank(message = "Операция должна быть подтверждена вводом текущего пароля")
  private String password;
}
