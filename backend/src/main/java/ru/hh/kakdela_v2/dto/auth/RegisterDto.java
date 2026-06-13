package ru.hh.kakdela_v2.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela_v2.util.ValidPassword;

@NoArgsConstructor
@Getter
@Setter
public class RegisterDto {

  @NotBlank(message = "Логин не должен быть пустым")
  @Max(value = 32, message = "Логин не должен быть длиннее 32 символов")
  private String login;
  @Email(message = "Электронная почта должна соответствовать формату")
  private String email;
  @ValidPassword
  private String password;
  private String passwordConfirmation;
}
