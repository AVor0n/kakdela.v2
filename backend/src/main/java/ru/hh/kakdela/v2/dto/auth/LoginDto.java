package ru.hh.kakdela.v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "LoginDto",
    title = "DTO для аутентификации пользователя"
)
public class LoginDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String login;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;
}