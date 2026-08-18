package ru.hh.kakdela.v2.util;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public final class CookieUtil {

  private static final String STRICT = "Strict";
  private static final String LAX = "Lax";

  // На http-стендах (без TLS) браузер отбрасывает Secure-cookie -
  // управляется через app.cookie.secure
  private static boolean secure;

  @Value("${app.cookie.secure:false}")
  private boolean secureProperty;

  @PostConstruct
  private void init() {
    secure = secureProperty;
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

  public static ResponseCookie buildHttpOnlyStrictCookie(
      String name,
      String value,
      String path,
      long maxAgeSeconds
  ) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite(STRICT)
        .path(path)
        .maxAge(maxAgeSeconds)
        .build();
  }

  public static ResponseCookie buildHttpOnlyLaxCookie(
      String name,
      String value,
      String path,
      long maxAgeSeconds
  ) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite(LAX)
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
        .secure(secure)
        .sameSite(STRICT)
        .path(path)
        .maxAge(maxAgeSeconds)
        .build();
  }

  public static ResponseCookie buildExpiredCookie(String name, String path) {
    return ResponseCookie.from(name, "")
        .httpOnly(true)
        .secure(secure)
        .sameSite(STRICT)
        .path(path)
        .maxAge(0)
        .build();
  }

  public static void addCookie(HttpServletResponse response, ResponseCookie cookie) {
    response.addHeader("Set-Cookie", cookie.toString());
  }
}
