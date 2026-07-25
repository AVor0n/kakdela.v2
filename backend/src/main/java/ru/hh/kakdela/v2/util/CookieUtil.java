package ru.hh.kakdela.v2.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;

public final class CookieUtil {

  public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
  public static final String DEVICE_ID_COOKIE_NAME = "deviceId";

  public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
  public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);
  public static final Duration DEVICE_ID_TTL = Duration.ofDays(365);

  private static final String ACCESS_TOKEN_PATH = "/";
  private static final String REFRESH_TOKEN_PATH = "/api/auth";
  private static final String SAME_SITE = "Strict";

  private CookieUtil() {
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

  public static void setAccessTokenCookie(HttpServletResponse response, String token) {
    setHttpOnlyCookie(
        response,
        ACCESS_TOKEN_PATH,
        ACCESS_TOKEN_TTL.getSeconds(),
        ACCESS_TOKEN_COOKIE_NAME,
        token
    );
  }

  public static void setRefreshTokenCookie(HttpServletResponse response, String token) {
    setHttpOnlyCookie(
        response,
        REFRESH_TOKEN_PATH,
        REFRESH_TOKEN_TTL.getSeconds(),
        REFRESH_TOKEN_COOKIE_NAME,
        token
    );
  }

  public static void setDeviceIdCookie(HttpServletResponse response, String deviceId) {
    ResponseCookie cookie = ResponseCookie.from(DEVICE_ID_COOKIE_NAME, deviceId)
        .httpOnly(false)
        .secure(true)
        .sameSite(SAME_SITE)
        .path(ACCESS_TOKEN_PATH)
        .maxAge(DEVICE_ID_TTL.getSeconds())
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  public static void clearAccessTokenCookie(HttpServletResponse response) {
    clearCookie(response, ACCESS_TOKEN_COOKIE_NAME, ACCESS_TOKEN_PATH);
  }

  public static void clearRefreshTokenCookie(HttpServletResponse response) {
    clearCookie(response, REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN_PATH);
  }

  public static void clearDeviceIdCookie(HttpServletResponse response) {
    clearCookie(response, DEVICE_ID_COOKIE_NAME, ACCESS_TOKEN_PATH);
  }

  public static void clearAllCookies(HttpServletResponse response) {
    clearAccessTokenCookie(response);
    clearRefreshTokenCookie(response);
    clearDeviceIdCookie(response);
  }

  public static void setHttpOnlyCookie(
      HttpServletResponse response,
      String path,
      long maxAgeSeconds,
      String name,
      String value) {

    ResponseCookie cookie = ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(true)
        .sameSite(SAME_SITE)
        .path(path)
        .maxAge(maxAgeSeconds)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  public static void clearCookie(HttpServletResponse response, String name, String path) {
    ResponseCookie cookie = ResponseCookie.from(name, "")
        .httpOnly(true)
        .secure(true)
        .sameSite(SAME_SITE)
        .path(path)
        .maxAge(0)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }
}
