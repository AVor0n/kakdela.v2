package ru.hh.kakdela.v2.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class DeviceUtil {

  public static String getDeviceId(HttpServletRequest request) {
    return CookieUtil.getDeviceId(request);
  }

  public static String generateDeviceId() {
    return UUID.randomUUID().toString();
  }
}
