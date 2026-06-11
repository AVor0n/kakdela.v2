package ru.hh.kakdela_v2.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.hh.kakdela_v2.util.ValidPassword;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {

  @NotBlank(message = "Логин не может быть пустым")
  private String login;
  @Email(message = "Электронная почта должна соответствовать формату")
  private String email;
  @ValidPassword
  private String password;
  private String passwordConfirmation;
}
