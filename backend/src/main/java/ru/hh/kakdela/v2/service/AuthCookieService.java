package ru.hh.kakdela.v2.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import ru.hh.kakdela.v2.util.CookieUtil;

@Service
public class AuthCookieService {

  public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
  public static final String DEVICE_ID_COOKIE_NAME = "deviceId";
  public static final String RESPONSE_TOKEN_PREFIX = "responseAccessToken_";
  public static final String HH_LINK_INTENT_COOKIE_NAME = "hhLinkIntent";
  public static final String HH_LINK_TOKEN_COOKIE_NAME = "hhLinkToken";

  public static final String MAIN_PATH = "/api";
  public static final String REFRESH_PATH = "/api/auth/refresh";
  public static final String HH_LINK_INTENT_PATH = "/api/auth/oauth2";
  public static final String HH_LINK_TOKEN_PATH = "/api/accounts/me/link-hh-sso";
  
  @Value("${app.tokens.hh-link.max-age}")
  private long hhLinkTokenMaxAge;

  @Value("${app.tokens.refresh.max-age}")
  private long refreshTokenMaxAge;

  @Value("${app.tokens.device-id.max-age}")
  private long deviceIdMaxAge;

  @Value("${app.tokens.response-access.max-age}")
  private long responseTokenMaxAge;

  public String getAccessToken(HttpServletRequest request) {
    return CookieUtil.getCookieValueByName(request, ACCESS_TOKEN_COOKIE_NAME);
  }

  public String getRefreshToken(HttpServletRequest request) {
    return CookieUtil.getCookieValueByName(request, REFRESH_TOKEN_COOKIE_NAME);
  }

  public String getDeviceId(HttpServletRequest request) {
    return CookieUtil.getCookieValueByName(request, DEVICE_ID_COOKIE_NAME);
  }

  public String getResponseToken(HttpServletRequest request, UUID responseId) {
    String cookieName = RESPONSE_TOKEN_PREFIX + responseId;
    return CookieUtil.getCookieValueByName(request, cookieName);
  }

  public void setAccessTokenCookie(HttpServletResponse response, String token) {
    ResponseCookie cookie = CookieUtil.buildHttpOnlyStrictCookie(
        ACCESS_TOKEN_COOKIE_NAME, token, MAIN_PATH, refreshTokenMaxAge
    );
    CookieUtil.addCookie(response, cookie);
  }

  public void setRefreshTokenCookie(HttpServletResponse response, String token) {
    ResponseCookie cookie = CookieUtil.buildHttpOnlyStrictCookie(
        REFRESH_TOKEN_COOKIE_NAME, token, REFRESH_PATH, refreshTokenMaxAge
    );
    CookieUtil.addCookie(response, cookie);
  }

  public void setDeviceIdCookie(HttpServletResponse response, String deviceId) {
    ResponseCookie cookie = CookieUtil.buildCookie(
        DEVICE_ID_COOKIE_NAME, deviceId, REFRESH_PATH, deviceIdMaxAge);
    CookieUtil.addCookie(response, cookie);
  }

  public void setResponseTokenCookie(HttpServletResponse response, UUID responseId, String token) {
    String cookieName = RESPONSE_TOKEN_PREFIX + responseId;
    ResponseCookie cookie = CookieUtil.buildHttpOnlyStrictCookie(
        cookieName, token, MAIN_PATH, responseTokenMaxAge);
    CookieUtil.addCookie(response, cookie);
  }

  public void setHhLinkIntentCookie(HttpServletResponse response, String accessToken) {
    ResponseCookie cookie = CookieUtil.buildHttpOnlyLaxCookie(
        HH_LINK_INTENT_COOKIE_NAME, accessToken, HH_LINK_INTENT_PATH, hhLinkTokenMaxAge);
    CookieUtil.addCookie(response, cookie);
  }

  public String consumeHhLinkIntentCookie(HttpServletRequest request,
                                          HttpServletResponse response) {
    String value = CookieUtil.getCookieValueByName(request, HH_LINK_INTENT_COOKIE_NAME);
    CookieUtil.addCookie(response,
        CookieUtil.buildExpiredCookie(HH_LINK_INTENT_COOKIE_NAME, HH_LINK_INTENT_PATH));
    return value;
  }

  public void setHhLinkTokenCookie(HttpServletResponse response, String hhLinkToken) {
    ResponseCookie cookie = CookieUtil.buildHttpOnlyStrictCookie(
        HH_LINK_TOKEN_COOKIE_NAME, hhLinkToken, HH_LINK_TOKEN_PATH, hhLinkTokenMaxAge);
    CookieUtil.addCookie(response, cookie);
  }

  public String consumeHhLinkTokenCookie(HttpServletRequest request,
                                         HttpServletResponse response) {
    String value = CookieUtil.getCookieValueByName(request, HH_LINK_TOKEN_COOKIE_NAME);
    CookieUtil.addCookie(response,
        CookieUtil.buildExpiredCookie(HH_LINK_TOKEN_COOKIE_NAME, HH_LINK_TOKEN_PATH));
    return value;
  }

  public void clearAccessTokenCookie(HttpServletResponse response) {
    CookieUtil.addCookie(response,
        CookieUtil.buildExpiredCookie(ACCESS_TOKEN_COOKIE_NAME, MAIN_PATH));
  }

  public void clearRefreshTokenCookie(HttpServletResponse response) {
    CookieUtil.addCookie(response,
        CookieUtil.buildExpiredCookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_PATH));
  }

  public void clearDeviceIdCookie(HttpServletResponse response) {
    CookieUtil.addCookie(response,
        CookieUtil.buildExpiredCookie(DEVICE_ID_COOKIE_NAME, REFRESH_PATH));
  }

  public void clearResponseTokenCookie(HttpServletResponse response, UUID responseId) {
    String cookieName = RESPONSE_TOKEN_PREFIX + responseId;
    CookieUtil.addCookie(response,
        CookieUtil.buildExpiredCookie(cookieName, MAIN_PATH));
  }

  public void clearAllAuthCookies(HttpServletResponse response) {
    clearAccessTokenCookie(response);
    clearRefreshTokenCookie(response);
    clearDeviceIdCookie(response);
  }

  public String getOrCreateDeviceId(HttpServletRequest request, HttpServletResponse response) {
    String deviceId = getDeviceId(request);
    if (deviceId == null) {
      deviceId = UUID.randomUUID().toString();
      setDeviceIdCookie(response, deviceId);
    }
    return deviceId;
  }
}
