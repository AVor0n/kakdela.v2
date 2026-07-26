package ru.hh.kakdela.v2.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public final class CookieUtil {

  public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
  public static final String DEVICE_ID_COOKIE_NAME = "deviceId";
  public static final String RESPONSE_TOKEN_PREFIX = "responseAccessToken_";

  @Value("${app.tokens.access.max-age}")
  private long accessTokenMaxAge;

  @Value("${app.tokens.refresh.max-age}")
  private long refreshTokenMaxAge;

  @Value("${app.tokens.device-id.max-age}")
  private long deviceIdMaxAge;

  @Value("${app.tokens.response-access.max-age}")
  private long responseTokenMaxAge;

  private static final String SAME_SITE = "Strict";

  public CookieUtil() {
  }

  public static String getCookieValueByName(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(cookie -> name.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }

  public static String getAccessToken(HttpServletRequest request) {
    return getCookieValueByName(request, ACCESS_TOKEN_COOKIE_NAME);
  }

  public static String getRefreshToken(HttpServletRequest request) {
    return getCookieValueByName(request, REFRESH_TOKEN_COOKIE_NAME);
  }

  public static String getDeviceId(HttpServletRequest request) {
    return getCookieValueByName(request, DEVICE_ID_COOKIE_NAME);
  }

  public void setAccessTokenCookie(HttpServletResponse response, String token) {
    setHttpOnlyCookie(
        response,
        accessTokenMaxAge,
        ACCESS_TOKEN_COOKIE_NAME,
        token
    );
  }

  public void setRefreshTokenCookie(HttpServletResponse response, String token) {
    setHttpOnlyCookie(
        response,
        refreshTokenMaxAge,
        REFRESH_TOKEN_COOKIE_NAME,
        token
    );
  }

  public void setDeviceIdCookie(HttpServletResponse response, String deviceId) {
    ResponseCookie cookie = ResponseCookie.from(DEVICE_ID_COOKIE_NAME, deviceId)
        .httpOnly(false)
        .secure(true)
        .sameSite(SAME_SITE)
        .maxAge(deviceIdMaxAge)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  public void setResponseTokenCookie(
      HttpServletResponse response,
      UUID responseId,
      String token
  ) {

    String cookieName = RESPONSE_TOKEN_PREFIX + responseId;
    setHttpOnlyCookie(
        response,
        responseTokenMaxAge,
        cookieName,
        token
    );
  }

  public static void clearAccessTokenCookie(HttpServletResponse response) {
    clearCookie(response, ACCESS_TOKEN_COOKIE_NAME);
  }

  public static void clearRefreshTokenCookie(HttpServletResponse response) {
    clearCookie(response, REFRESH_TOKEN_COOKIE_NAME);
  }

  public static void clearDeviceIdCookie(HttpServletResponse response) {
    clearCookie(response, DEVICE_ID_COOKIE_NAME);
  }

  public static void clearAllAuthCookies(HttpServletResponse response) {
    clearAccessTokenCookie(response);
    clearRefreshTokenCookie(response);
    clearDeviceIdCookie(response);
  }

  public static void setHttpOnlyCookie(
      HttpServletResponse response,
      long maxAgeSeconds,
      String name,
      String value) {

    ResponseCookie cookie = ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(true)
        .sameSite(SAME_SITE)
        .maxAge(maxAgeSeconds)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  public static void clearCookie(HttpServletResponse response, String name) {
    ResponseCookie cookie = ResponseCookie.from(name, "")
        .httpOnly(true)
        .secure(true)
        .sameSite(SAME_SITE)
        .maxAge(0)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  public static void clearResponseTokenCookie(
      HttpServletResponse response,
      UUID responseId) {

    String cookieName = RESPONSE_TOKEN_PREFIX + responseId;
    clearCookie(response, cookieName);
  }

  public static String getResponseToken(
      HttpServletRequest request,
      UUID responseId) {

    String cookieName = RESPONSE_TOKEN_PREFIX + responseId;
    return getCookieValueByName(request, cookieName);
  }
}
