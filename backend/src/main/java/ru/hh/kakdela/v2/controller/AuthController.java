package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.account.AccountCreateDto;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.dto.auth.PasswordResetDto;
import ru.hh.kakdela.v2.dto.auth.VerifyCodeRequestDto;
import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela.v2.dto.auth.LoginDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.AccountService;
import ru.hh.kakdela.v2.service.AuthCookieService;
import ru.hh.kakdela.v2.service.AuthService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Authentication", description = "Регистрация и вход")
public class AuthController {

  private final AuthService authService;
  private final AccountService accountService;
  private final AuthCookieService authCookieService;

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

    String deviceId = authCookieService.getOrCreateDeviceId(request, response);

    String userAgent = request.getHeader("User-Agent");
    String ipAddress = request.getRemoteAddr();

    AuthTokensDto tokens = authService.login(dto, deviceId, userAgent, ipAddress);

    authCookieService.setAccessTokenCookie(response, tokens.getAccessToken());
    authCookieService.setRefreshTokenCookie(response, tokens.getRefreshToken());
  }

  @PostMapping("/auth/refresh")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void refresh(
      HttpServletRequest request,
      HttpServletResponse response) {

    String refreshToken = authCookieService.getRefreshToken(request);
    String deviceId = authCookieService.getDeviceId(request);
    String userAgent = request.getHeader("User-Agent");
    String ipAddress = request.getRemoteAddr();

    AuthTokensDto tokens = authService.refreshTokens(
        refreshToken,
        deviceId,
        userAgent,
        ipAddress);

    authCookieService.setAccessTokenCookie(response, tokens.getAccessToken());
    authCookieService.setRefreshTokenCookie(response, tokens.getRefreshToken());
  }

  @PostMapping("/auth/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response) {

    String refreshToken = authCookieService.getRefreshToken(request);
    authService.logout(refreshToken);
    authCookieService.clearAllAuthCookies(response);
  }

  @PostMapping("/auth/logout-everywhere")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logoutEverywhere(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      HttpServletResponse response) {

    authService.logoutEverywhere(userDetails.getId());

    authCookieService.clearAllAuthCookies(response);
  }

  @PostMapping("/auth/forgot-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void sendPasswordResetEmail(String email) {
    authService.sendPasswordResetEmail(email);
  }

  @GetMapping("/auth/verify-reset-code")
  public ResponseEntity<Boolean> verifyResetCode(VerifyCodeRequestDto dto) {
    return ResponseEntity.ok(authService.verifyResetCode(dto));
  }

  @PatchMapping("/auth/reset-password")
  public void resetPassword(PasswordResetDto dto) {
    authService.resetPassword(dto);
  }
}
