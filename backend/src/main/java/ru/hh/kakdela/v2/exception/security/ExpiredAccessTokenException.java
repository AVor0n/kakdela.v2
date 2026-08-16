package ru.hh.kakdela.v2.exception.security;

import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2AuthenticationException;

public class ExpiredAccessTokenException extends Kd2AuthenticationException {
  public ExpiredAccessTokenException(Throwable cause) {
    super(ErrorCode.EXPIRED_ACCESS_TOKEN,
        "Срок действия предоставленного access token истёк", null, cause);
  }
}
