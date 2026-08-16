package ru.hh.kakdela.v2.exception;

import org.springframework.http.HttpStatus;

public class BadRequestDataException extends Kd2Exception {
  public BadRequestDataException(String details) {
    super(ErrorCode.BAD_REQUEST_DATA, HttpStatus.BAD_REQUEST, details);
  }
}
