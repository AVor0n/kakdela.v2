package ru.hh.kakdela.v2.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import ru.hh.kakdela.v2.util.CookieUtil;

@Component
public class CookieOauth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String COOKIE_NAME = "oauth2AuthRequest";
  private static final String COOKIE_PATH = "/";
  private static final long COOKIE_MAX_AGE = 180;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    String value = CookieUtil.getCookieValueByName(request, COOKIE_NAME);
    return value == null ? null : deserialize(value);
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (authorizationRequest == null) {
      CookieUtil.addCookie(response, CookieUtil.buildExpiredCookie(COOKIE_NAME, COOKIE_PATH));
      return;
    }
    ResponseCookie cookie = CookieUtil.buildLaxCookie(
        COOKIE_NAME, serialize(authorizationRequest), COOKIE_PATH, COOKIE_MAX_AGE);
    CookieUtil.addCookie(response, cookie);
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
    CookieUtil.addCookie(response, CookieUtil.buildExpiredCookie(COOKIE_NAME, COOKIE_PATH));
    return authorizationRequest;
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
