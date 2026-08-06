package ru.hh.kakdela.v2.exception;

import lombok.Getter;

@Getter
public class Kd2Exception extends RuntimeException {

  private final ErrorCode errorCode;

  protected Kd2Exception(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
