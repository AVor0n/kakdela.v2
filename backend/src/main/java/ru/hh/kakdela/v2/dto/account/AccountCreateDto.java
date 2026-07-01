package ru.hh.kakdela.v2.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela_v2.util.ValidPassword;

@NoArgsConstructor
@Getter
@Setter
public class AccountCreateDto {

  @NotBlank(message = "Логин не должен быть пустым")
  @Size(max = 32, message = "Логин не должен быть длиннее 32 символов")
  private String login;
  @NotBlank(message = "Электронная почта не должна быть пустой")
  @Email(message = "Электронная почта должна соответствовать формату")
  private String email;
  @NotBlank(message = "Пароль не должен быть пустым")
  @ValidPassword
  private String password;
  private String passwordConfirmation;
}
