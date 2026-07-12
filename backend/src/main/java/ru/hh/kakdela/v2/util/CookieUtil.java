package ru.hh.kakdela.v2.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

  public static String getCookieValueByName(
      HttpServletRequest request, String name) {

    return request.getCookies() == null
        ? null
        : Arrays.stream(request.getCookies())
            .filter(c -> c.getName().equals(name))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
  }

  public static void setHttpOnlySameSiteStrictCookie(
      HttpServletResponse response, String path,
      long maxAgeSeconds, String name, String value) {

    ResponseCookie cookie = ResponseCookie.from(name, value)
        .httpOnly(true)
        .sameSite("Strict")
        .path(path)
        .maxAge(maxAgeSeconds)
        .build();

    response.addHeader("Set-Cookie", cookie.toString());
  }

  public static void setHttpOnlySameSiteStrictCookie(
      HttpServletResponse response, String path,
      long maxAgeSeconds, String name) {

    ResponseCookie cookie = ResponseCookie.from(name)
        .httpOnly(true)
        .sameSite("Strict")
        .path(path)
        .maxAge(maxAgeSeconds)
        .build();

    response.addHeader("Set-Cookie", cookie.toString());
  }
}
