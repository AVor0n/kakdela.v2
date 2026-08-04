package ru.hh.kakdela.v2.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

@Component
public class CookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String COOKIE_NAME = "oauth2AuthRequest";
  private static final long COOKIE_MAX_AGE = 180;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (COOKIE_NAME.equals(cookie.getName())) {
        return deserialize(cookie.getValue());
      }
    }
    return null;
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (authorizationRequest == null) {
      addCookie(response, "", 0);
      return;
    }
    addCookie(response, serialize(authorizationRequest), COOKIE_MAX_AGE);
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
    addCookie(response, "", 0);
    return authorizationRequest;
  }

  private void addCookie(HttpServletResponse response, String value, long maxAgeSeconds) {
    ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(true)
        .sameSite("Lax")
        .path("/")
        .maxAge(maxAgeSeconds)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private static String serialize(OAuth2AuthorizationRequest authorizationRequest) {
    return Base64.getUrlEncoder()
        .encodeToString(SerializationUtils.serialize(authorizationRequest));
  }

  private static OAuth2AuthorizationRequest deserialize(String value) {
    return (OAuth2AuthorizationRequest) SerializationUtils
        .deserialize(Base64.getUrlDecoder().decode(value));
  }
}
