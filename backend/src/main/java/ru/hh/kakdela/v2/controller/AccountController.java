package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.account.AccountDeleteDto;
import ru.hh.kakdela.v2.dto.account.AccountPatchDto;
import ru.hh.kakdela.v2.dto.account.AccountPutDto;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.AccountService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Управление аккаунтами")
public class AccountController {

  private final AccountService accountService;

  @GetMapping("/accounts/me")
  public AccountResponseDto getMyAccount(
      @AuthenticationPrincipal CustomUserDetails authenticatedUser) {
    return accountService.getById(authenticatedUser.getId());
  }

  @PutMapping("/accounts/me")
  public AccountResponseDto replaceMyAccount(
      @Valid @RequestBody AccountPutDto accountPutDto,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser) {
    return accountService.updateFull(authenticatedUser, accountPutDto);
  }

  @PatchMapping("/accounts/me")
  public AccountResponseDto patchMyAccount(
      @Valid @RequestBody AccountPatchDto accountPatchDto,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser) {
    return accountService.updatePartial(authenticatedUser, accountPatchDto);
  }

  @PostMapping("/accounts/me/delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMyAccount(
      @Valid @RequestBody AccountDeleteDto accountDeleteDto,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser,
      HttpServletResponse response) {
    accountService.delete(authenticatedUser, accountDeleteDto);

    ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken")
        .httpOnly(true)
        .sameSite("Strict")
        .path("/api")
        .maxAge(0)
        .build();

    ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken")
        .httpOnly(true)
        .sameSite("Strict")
        .path("/api/auth/refresh")
        .maxAge(0)
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
  }
}
