package ru.hh.kakdela.v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(
    name = "AuthTokensDto",
    title = "DTO с токенами аутентификации"
)
public class AuthTokensDto {

  private final String accessToken;
  private final String refreshToken;
}