package ru.hh.kakdela.v2.exception.condition;

import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;

public class ConditionShouldNavigateForwardException extends Kd2Exception {
  public ConditionShouldNavigateForwardException() {
    super(ErrorCode.CONDITION_SHOULD_NAVIGATE_FORWARD, HttpStatus.BAD_REQUEST,
        "Правила перехода должны перенаправлять только вперёд");
  }
}
