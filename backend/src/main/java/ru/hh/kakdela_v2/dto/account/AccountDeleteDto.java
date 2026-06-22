package ru.hh.kakdela_v2.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountDeleteDto {
  @NotBlank(message = "Операция должна быть подтверждена вводом текущего пароля")
  private String password;
}
