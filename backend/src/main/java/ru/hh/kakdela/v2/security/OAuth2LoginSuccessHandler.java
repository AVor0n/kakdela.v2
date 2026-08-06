package ru.hh.kakdela.v2.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  // Подтверждено реальным ответом GET /me для соискателя (auth_type=applicant).
  private static final String ATTR_EMAIL = "email";

  private final AccountService accountService;
  private final AuthService authService;
  private final AuthCookieService authCookieService;

  @Value("${app.oauth2.frontend-redirect-uri}")
  private String frontendRedirectUri;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute(ATTR_EMAIL);

    try {
      if (email == null || email.isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "hh.ru не передал email в ответе");
      }

      Account account = accountService.findOrCreateByHhSso(email);

      String deviceId = authCookieService.getOrCreateDeviceId(request, response);
      String userAgent = request.getHeader("User-Agent");
      String ipAddress = request.getRemoteAddr();

      AuthTokensDto tokens = authService.issueTokens(account, deviceId, userAgent, ipAddress);
      authCookieService.setAccessTokenCookie(response, tokens.getAccessToken());
      authCookieService.setRefreshTokenCookie(response, tokens.getRefreshToken());

      log.info("Выполнен вход через hh.ru login={}", account.getLogin());
      response.sendRedirect(frontendRedirectUri);
    } catch (ResponseStatusException ex) {
      log.warn("Не удалось войти через hh.ru: {}", ex.getReason());
      response.sendRedirect(buildErrorRedirect());
    }
  }

  private String buildErrorRedirect() {
    return UriComponentsBuilder.fromUriString(frontendRedirectUri)
        .queryParam("error", "hh_login_failed")
        .build()
        .toUriString();
  }
}
