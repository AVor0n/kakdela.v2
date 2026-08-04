package ru.hh.kakdela.v2.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;

public final class CookieUtil {

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

  public static ResponseCookie buildHttpOnlyCookie(
      String name,
      String value,
      String path,
      long maxAgeSeconds
  ) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(true)
        .sameSite(SAME_SITE)
        .path(path)
        .maxAge(maxAgeSeconds)
        .build();
  }

  public static ResponseCookie buildCookie(
      String name,
      String value,
      String path,
      long maxAgeSeconds
  ) {
    return ResponseCookie.from(name, value)
        .httpOnly(false)
        .secure(true)
        .sameSite(SAME_SITE)
        .path(path)
        .maxAge(maxAgeSeconds)
        .build();
  }

  public static ResponseCookie buildExpiredCookie(String name, String path) {
    return ResponseCookie.from(name, "")
        .httpOnly(true)
        .secure(true)
        .sameSite(SAME_SITE)
        .path(path)
        .maxAge(0)
        .build();
  }

  public static void addCookie(HttpServletResponse response, ResponseCookie cookie) {
    response.addHeader("Set-Cookie", cookie.toString());
  }
}
