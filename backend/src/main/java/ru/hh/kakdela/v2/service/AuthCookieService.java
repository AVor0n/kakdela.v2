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

  public static final String MAIN_PATH = "/api";
  public static final String REFRESH_PATH = "/api/auth/refresh";

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
