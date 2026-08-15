package ru.hh.kakdela.v2.exception.condition;

import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;

public class ConditionTreeHeightLimitReachedException extends Kd2Exception {
  public ConditionTreeHeightLimitReachedException() {
    super(ErrorCode.CONDITION_TREE_HEIGHT_LIMIT_REACHED, HttpStatus.CONFLICT,
        "Превышена максимальная высота дерева правила перехода");
  }
}
