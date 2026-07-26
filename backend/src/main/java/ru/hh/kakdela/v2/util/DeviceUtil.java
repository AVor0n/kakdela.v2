package ru.hh.kakdela.v2.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class DeviceUtil {

  private final CookieUtil cookieUtil;

  public String getDeviceId(HttpServletRequest request) {
    return CookieUtil.getDeviceId(request);
  }

  public String getOrCreateDeviceId(HttpServletRequest request, HttpServletResponse response) {
    String deviceId = getDeviceId(request);
    if (deviceId == null) {
      deviceId = generateDeviceId();
      cookieUtil.setDeviceIdCookie(response, deviceId);
    }
    return deviceId;
  }

  public String generateDeviceId() {
    return UUID.randomUUID().toString();
  }
}
