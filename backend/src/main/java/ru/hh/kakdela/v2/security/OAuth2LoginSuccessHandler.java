package ru.hh.kakdela.v2.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.hh.kakdela.v2.constants.CookieNames;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.service.AccountService;
import ru.hh.kakdela.v2.util.CookieUtil;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private static final String ATTR_ID = "id";
  private static final String ATTR_EMAIL = "email";

  private final AccountService accountService;
  private final JwtService jwtService;

  @Value("${app.tokens.access.max-age}")
  private long accessTokenMaxAge;

  @Value("${app.oauth2.frontend-redirect-uri}")
  private String frontendRedirectUri;

  @Override
  public void onAuthenticationSuccess(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String hhUserId = String.valueOf(
        Objects.requireNonNull(Objects.requireNonNull(oAuth2User).getAttribute(ATTR_ID)));
    String email = oAuth2User.getAttribute(ATTR_EMAIL);

    try {
      if (email == null || email.isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "hh.ru не передал email в ответе");
      }

      Account account = accountService.findOrCreateByHhSso(hhUserId, email);

      String accessToken = jwtService.generateAccessToken(account.getLogin());
      CookieUtil.setHttpOnlySameSiteStrictCookie(
          response, "/api", accessTokenMaxAge, CookieNames.accessToken, accessToken);

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
