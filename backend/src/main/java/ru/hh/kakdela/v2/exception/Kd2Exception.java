package ru.hh.kakdela.v2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class Kd2Exception extends RuntimeException {

  private final ErrorCode errorCode;
  private final HttpStatus httpStatus;

  protected Kd2Exception(ErrorCode errorCode, HttpStatus httpStatus, String message) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }
}
