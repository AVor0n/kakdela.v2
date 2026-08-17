package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import ru.hh.kakdela.v2.dto.account.HhLinkConfirmDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.AccountService;
import ru.hh.kakdela.v2.service.AuthCookieService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Управление аккаунтами")
public class AccountController {

  private final AccountService accountService;
  private final AuthCookieService authCookieService;
  @Value("${app.oauth2.authorization-base-uri:/api/auth/oauth2/authorization}")
  private String oauth2AuthorizationBaseUri;


  @GetMapping("/accounts/me")
  public AccountResponseDto getMyAccount(@AuthenticationPrincipal CustomUserDetails currentUser) {
    return accountService.getById(currentUser.getId());
  }

  @PutMapping("/accounts/me")
  public AccountResponseDto replaceMyAccount(
      @Valid @RequestBody AccountPutDto accountPutDto,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return accountService.updateFull(currentUser, accountPutDto);
  }

  @PatchMapping("/accounts/me")
  public AccountResponseDto patchMyAccount(
      @Valid @RequestBody AccountPatchDto accountPatchDto,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return accountService.updatePartial(currentUser, accountPatchDto);
  }

  @PostMapping("/accounts/me/delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMyAccount(
      @Valid @RequestBody AccountDeleteDto accountDeleteDto,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletResponse response) {

    accountService.softDelete(currentUser, accountDeleteDto);
    authCookieService.clearAllAuthCookies(response);
  }

  @GetMapping("/accounts/me/link-hh-sso/init")
  public void initLinkHhSso(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String accessToken = authCookieService.getAccessToken(request);
    authCookieService.setHhLinkIntentCookie(response, accessToken);
    response.sendRedirect(oauth2AuthorizationBaseUri + "/hh");
  }

  // ВАЖНО: этот путь должен быть добавлен в permitAll в SecurityConfig - его обязан уметь
  // вызывать анонимный пользователь (сценарий 1, email-конфликт без активной сессии).
  // @AuthenticationPrincipal здесь принципиально nullable: Spring Security не бросит 401
  // сам, если маршрут в permitAll, а внутри accountService.confirmLinkHhSso сами решаем,
  // авторизован пользователь или нет.
  @PostMapping("/accounts/me/link-hh-sso/confirm")
  public AccountResponseDto confirmLinkHhSso(
      @Valid @RequestBody HhLinkConfirmDto dto,
      @Nullable @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletRequest request,
      HttpServletResponse response) {
    return accountService.confirmLinkHhSso(
        dto, currentUser != null ? currentUser.getId() : null, request, response);
  }
}
