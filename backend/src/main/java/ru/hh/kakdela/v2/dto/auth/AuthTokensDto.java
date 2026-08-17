package ru.hh.kakdela.v2.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthTokensDto {

  private final String accessToken;
  private final String refreshToken;
}