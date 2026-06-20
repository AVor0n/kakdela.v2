package ru.hh.kakdela_v2.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela_v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela_v2.dto.auth.LoginDto;
import ru.hh.kakdela_v2.dto.auth.RegisterDto;
import ru.hh.kakdela_v2.service.AuthService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthController {

  private final AuthService authService;

  @Value("${app.tokens.access.max-age}")
  private long accessTokenMaxAge;

  @Value("${app.tokens.refresh.max-age}")
  private long refreshTokenMaxAge;

  @PostMapping("/auth/register")
  @ResponseStatus(HttpStatus.CREATED)
  public void register(@Valid @RequestBody RegisterDto registerDto) {
    authService.register(registerDto);
  }

  @PostMapping("/auth/login")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
    AuthTokensDto authTokensDto = authService.login(loginDto);

    if (authTokensDto.getAccessToken() != null) {
      ResponseCookie accessTokenCookie = ResponseCookie.from(
              "accessToken", authTokensDto.getAccessToken())
          .httpOnly(true)
          .sameSite("Strict")
          .path("/api")
          .maxAge(accessTokenMaxAge)
          .build();

      response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
    }

    if (authTokensDto.getRefreshToken() != null) {
      ResponseCookie refreshTokenCookie = ResponseCookie.from(
              "refreshToken", authTokensDto.getRefreshToken())
          .httpOnly(true)
          .sameSite("Strict")
          .path("/api/auth/refresh")
          .maxAge(refreshTokenMaxAge)
          .build();

      response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
  }
}
