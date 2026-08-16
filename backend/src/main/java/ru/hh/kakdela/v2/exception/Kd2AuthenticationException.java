package ru.hh.kakdela.v2.exception;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.AuthenticationException;

@Getter
public class Kd2AuthenticationException extends AuthenticationException {

  private final ErrorCode errorCode;
  private final String objectDetails;

  public Kd2AuthenticationException(@Nullable String msg, String objectDetails) {
    super(msg);
    this.errorCode = ErrorCode.AUTHENTICATION_ERROR;
    this.objectDetails = objectDetails;
  }

  public Kd2AuthenticationException(
      ErrorCode errorCode,
      @Nullable String msg,
      @Nullable String objectDetails
  ) {
    super(msg);
    this.errorCode = errorCode;
    this.objectDetails = objectDetails;
  }

  public Kd2AuthenticationException(
      ErrorCode errorCode,
      @Nullable String msg,
      @Nullable String objectDetails,
      Throwable cause
  ) {
    super(msg, cause);
    this.errorCode = errorCode;
    this.objectDetails = objectDetails;
  }
}
