package ru.hh.kakdela.v2.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public final class DeviceUtil {

  private DeviceUtil() {
  }

  public static String getDeviceId(HttpServletRequest request) {
    return CookieUtil.getCookieValueByName(request, "deviceId");
  }

  public static String generateDeviceId() {
    return UUID.randomUUID().toString();
  }
}
