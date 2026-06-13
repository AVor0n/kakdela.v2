package ru.hh.kakdela_v2.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponseDto {

  private final String accessToken;
  private final String refreshToken;
}