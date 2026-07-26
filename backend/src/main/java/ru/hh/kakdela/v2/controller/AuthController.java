package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.account.AccountCreateDto;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela.v2.dto.auth.LoginDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.AccountService;
import ru.hh.kakdela.v2.service.AuthService;
import ru.hh.kakdela.v2.util.CookieUtil;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Authentication", description = "Регистрация и вход")
public class AuthController {

  private final AuthService authService;
  private final AccountService accountService;
  private final CookieUtil cookieUtil;

  @PostMapping("/auth/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AccountResponseDto register(@Valid @RequestBody AccountCreateDto dto) {
    return accountService.create(dto);
  }

  @PostMapping("/auth/login")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void login(
      @RequestBody LoginDto dto,
      HttpServletRequest request,
      HttpServletResponse response) {

    AuthTokensDto tokens = authService.login(dto, request, response);

    cookieUtil.setAccessTokenCookie(response, tokens.getAccessToken());
    cookieUtil.setRefreshTokenCookie(response, tokens.getRefreshToken());
  }

  @PostMapping("/auth/refresh")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void refresh(
      HttpServletRequest request,
      HttpServletResponse response) {

    AuthTokensDto tokens = authService.refreshTokens(request);

    cookieUtil.setAccessTokenCookie(response, tokens.getAccessToken());
    cookieUtil.setRefreshTokenCookie(response, tokens.getRefreshToken());
  }

  @PostMapping("/auth/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response) {
    authService.logout(request);

    CookieUtil.clearAllAuthCookies(response);
  }

  @PostMapping("/auth/logout-everywhere")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logoutEverywhere(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      HttpServletResponse response) {

    authService.logoutEverywhere(userDetails.getId());

    CookieUtil.clearAllAuthCookies(response);
  }
}
