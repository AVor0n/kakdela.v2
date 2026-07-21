package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.constants.CookieNames;
import ru.hh.kakdela.v2.dto.account.AccountCreateDto;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela.v2.dto.auth.LoginDto;
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

  @Value("${app.tokens.access.max-age}")
  private long accessTokenMaxAge;

  @Value("${app.tokens.refresh.max-age}")
  private long refreshTokenMaxAge;

  @PostMapping("/auth/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AccountResponseDto register(@Valid @RequestBody AccountCreateDto dto) {

    return accountService.create(dto);
  }

  @PostMapping("/auth/login")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void login(
      @RequestBody LoginDto dto,
      HttpServletResponse response
  ) {

    AuthTokensDto authTokensDto = authService.login(dto);

    CookieUtil.setHttpOnlySameSiteStrictCookie(
        response, "/api", accessTokenMaxAge,
        CookieNames.accessToken, authTokensDto.getAccessToken());
    CookieUtil.setHttpOnlySameSiteStrictCookie(
        response, "/api", refreshTokenMaxAge,
        CookieNames.refreshToken, authTokensDto.getRefreshToken());
  }

  @PostMapping("/auth/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(HttpServletResponse response) {

    CookieUtil.setHttpOnlySameSiteStrictCookie(
        response, "/api", 0, CookieNames.accessToken);
    CookieUtil.setHttpOnlySameSiteStrictCookie(
        response, "/api", 0, CookieNames.refreshToken);
  }
}
