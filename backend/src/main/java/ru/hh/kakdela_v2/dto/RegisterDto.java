package ru.hh.kakdela_v2.dto;

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

  @NotBlank
  private String login;
  @Email
  private String email;
  @ValidPassword
  private String password;
  private String passwordConfirmation;
}
