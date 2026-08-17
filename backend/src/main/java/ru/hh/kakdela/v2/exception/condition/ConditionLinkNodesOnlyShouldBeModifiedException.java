package ru.hh.kakdela.v2.exception.condition;

import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;

public class ConditionLinkNodesOnlyShouldBeModifiedException extends Kd2Exception {
  public ConditionLinkNodesOnlyShouldBeModifiedException() {
    super(ErrorCode.CONDITION_LINK_NODES_ONLY_SHOULD_BE_MODIFIED, HttpStatus.BAD_REQUEST,
        "Изменять можно только соединительные вершины");
  }
}
