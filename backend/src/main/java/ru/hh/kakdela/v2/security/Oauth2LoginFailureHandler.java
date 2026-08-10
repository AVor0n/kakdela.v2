package ru.hh.kakdela.v2.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class Oauth2LoginFailureHandler implements AuthenticationFailureHandler {

  @Value("${app.oauth2.frontend-redirect-uri}")
  private String frontendRedirectUri;

  @Override
  public void onAuthenticationFailure(
      @NonNull HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception) throws IOException {

    log.warn("Ошибка входа через hh.ru: {}", exception.getMessage());

    String redirectWithError = UriComponentsBuilder.fromUriString(frontendRedirectUri)
        .queryParam("error", "hh_login_failed")
        .build()
        .toUriString();
    response.sendRedirect(redirectWithError);
  }
}
