package ru.hh.kakdela.v2.exception.condition;

import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;

public class ConditionNodeOperatorIsNotLinkException extends Kd2Exception {
  public ConditionNodeOperatorIsNotLinkException() {
    super(ErrorCode.CONDITION_NODE_OPERATOR_IS_NOT_LINK, HttpStatus.BAD_REQUEST,
        "Оператор вершины не является соединительным");
  }
}
