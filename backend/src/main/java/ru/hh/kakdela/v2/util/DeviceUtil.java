package ru.hh.kakdela.v2.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public final class DeviceUtil {

  private DeviceUtil() {
  }

  public static String getDeviceId(HttpServletRequest request) {
    return CookieUtil.getDeviceId(request);
  }

  public static String getOrCreateDeviceId(HttpServletRequest request, HttpServletResponse response) {
    String deviceId = getDeviceId(request);
    if (deviceId == null) {
      deviceId = generateDeviceId();
      CookieUtil.setDeviceIdCookie(response, deviceId);
    }
    return deviceId;
  }

  public static String generateDeviceId() {
    return UUID.randomUUID().toString();
  }
}
