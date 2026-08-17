package ru.hh.kakdela.v2.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.service.AccountService;
import ru.hh.kakdela.v2.service.AuthCookieService;
import ru.hh.kakdela.v2.service.AuthService;

@Slf4j
@RequiredArgsConstructor
@Component
public class Oauth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private static final String ATTR_EMAIL = "email";
  private static final String ATTR_HH_USER_ID = "id";

  private final AccountService accountService;
  private final AuthService authService;
  private final AuthCookieService authCookieService;
  private final JwtService jwtService;

  @Value("${app.oauth2.frontend-redirect-uri}")
  private String frontendRedirectUri;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    String email = oauth2User.getAttribute(ATTR_EMAIL);
    Object rawHhUserId = oauth2User.getAttributes().get(ATTR_HH_USER_ID);
    String hhUserId = rawHhUserId == null ? null : String.valueOf(rawHhUserId);

    // Кука ставится только явным флоу "привязать hh к текущему аккаунту" (/link-hh-sso/init).
    // Она несёт сам accessToken (через SameSite=Lax, т.к. сам accessToken - Strict и не долетает
    // до кросс-сайтового колбэка). Если её нет - значит пришли через обычную кнопку логина.
    String carriedAccessToken = authCookieService.consumeHhLinkIntentCookie(request, response);
    boolean isExplicitLinkFlow = carriedAccessToken != null;

    try {
      if (hhUserId == null || hhUserId.isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "hh.ru не передал id пользователя");
      }

      if (isExplicitLinkFlow) {
        handleExplicitLink(carriedAccessToken, hhUserId);
        response.sendRedirect(buildRedirect("hh_link", "success"));
        return;
      }

      Optional<Account> existingByHhUserId = accountService.findByHhUserId(hhUserId);
      if (existingByHhUserId.isPresent()) {
        // hh-аккаунт уже привязан к кому-то - это просто повторный вход через hh.ru
        handleLogin(request, response, existingByHhUserId.get());
        return;
      }

      if (email == null || email.isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "hh.ru не передал email в ответе");
      }

      if (accountService.existsByEmail(email)) {
        String hhLinkToken = jwtService.generateHhLinkToken(email, hhUserId);
        response.sendRedirect(buildRedirect("hh_link_token", hhLinkToken));
        return;
      }

      handleNewSsoSignup(request, response, hhUserId, email);

    } catch (ResponseStatusException ex) {
      log.warn("Ошибка при обработке {} через hh.ru: {}",
          isExplicitLinkFlow ? "привязки" : "входа", ex.getReason());
      response.sendRedirect(buildErrorRedirect(isExplicitLinkFlow));
    }
  }

  private void handleExplicitLink(String carriedAccessToken, String hhUserId) {
    UUID accountId = authService.resolveAccountIdFromToken(carriedAccessToken)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
            "Сессия истекла, повторите привязку"));

    accountService.linkHhSso(accountId, hhUserId);
    log.info("Аккаунт id={} привязан к hh.ru через колбэк", accountId);
  }

  private void handleNewSsoSignup(HttpServletRequest request, HttpServletResponse response,
                                  String hhUserId, String email) throws IOException {
    Account account = accountService.findOrCreateByHhSso(hhUserId, email);
    handleLogin(request, response, account);
  }

  private void handleLogin(HttpServletRequest request, HttpServletResponse response,
                           Account account) throws IOException {
    String deviceId = authCookieService.getOrCreateDeviceId(request, response);
    String userAgent = request.getHeader("User-Agent");
    String ipAddress = request.getRemoteAddr();

    AuthTokensDto tokens = authService.issueTokens(account, deviceId, userAgent, ipAddress);
    authCookieService.setAccessTokenCookie(response, tokens.getAccessToken());
    authCookieService.setRefreshTokenCookie(response, tokens.getRefreshToken());

    log.info("Выполнен вход через hh.ru login={}", account.getLogin());
    response.sendRedirect(frontendRedirectUri);
  }

  private String buildRedirect(String paramName, String paramValue) {
    return UriComponentsBuilder.fromUriString(frontendRedirectUri)
        .queryParam(paramName, paramValue)
        .build()
        .toUriString();
  }

  private String buildErrorRedirect(boolean isLinkFlow) {
    return buildRedirect("error", isLinkFlow ? "hh_link_failed" : "hh_login_failed");
  }
}
