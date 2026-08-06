package ru.hh.kakdela.v2.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultValues {

  // Survey / NotificationSchedule
  public static final Boolean IS_AUTHORIZED_ONLY_DEFAULT = false;
  public static final Boolean IS_LIMITED_TO_ONE_RESPONSE_DEFAULT = false;
  public static final Boolean DO_NOTIFY_DEFAULT = true;
  public static final String TARGET_TIMEZONE_DEFAULT = "Europe/Moscow";
}
