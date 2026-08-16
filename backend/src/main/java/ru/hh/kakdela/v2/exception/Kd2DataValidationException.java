package ru.hh.kakdela.v2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class Kd2DataValidationException extends Kd2Exception {

  private final String fieldName;
  private final String constraintMessage;

  public Kd2DataValidationException(String fieldName, String constraintMessage) {
    super(ErrorCode.DATA_CONSTRAINT_VIOLATION, HttpStatus.BAD_REQUEST,
        "Значение поля \"" + fieldName + "\" нарушает ограничение: " + constraintMessage);
    this.fieldName = fieldName;
    this.constraintMessage = constraintMessage;
  }
}
