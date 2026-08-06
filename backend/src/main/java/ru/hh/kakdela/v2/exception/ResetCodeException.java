package ru.hh.kakdela.v2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResetCodeException extends RuntimeException{
  private final String reason = "Ошибка при проверке кода подтверждения";
  private final String message;
  private final int remainingAttempts;
  private final HttpStatus statusCode;

  public ResetCodeException(String message, int remainingAttempts, HttpStatus status) {
    super(message);
    this.message = message;
    this.remainingAttempts = remainingAttempts;
    this.statusCode = status;
  }

}
