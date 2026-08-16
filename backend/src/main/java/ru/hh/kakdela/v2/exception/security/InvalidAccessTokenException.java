package ru.hh.kakdela.v2.exception.security;

import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2AuthenticationException;

public class InvalidAccessTokenException extends Kd2AuthenticationException {
  public InvalidAccessTokenException(Throwable cause) {
    super(ErrorCode.INVALID_ACCESS_TOKEN,
        "Предоставленный access token недействителен", null, cause);
  }
}
