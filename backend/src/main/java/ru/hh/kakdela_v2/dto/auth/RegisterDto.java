package ru.hh.kakdela_v2.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela_v2.validator.ValidPassword;

@NoArgsConstructor
@Getter
@Setter
public class RegisterDto {

  @NotBlank(message = "Логин не должен быть пустым")
  @Size(max = 32, message = "Логин не должен быть длиннее 32 символов")
  private String login;
  @Email(message = "Электронная почта должна соответствовать формату")
  private String email;
  @ValidPassword
  private String password;
  private String passwordConfirmation;
}
