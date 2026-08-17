package ru.hh.kakdela.v2.exception.condition;

import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;

public class ConditionTreeIsNotEmptyException extends Kd2Exception {
  public ConditionTreeIsNotEmptyException() {
    super(ErrorCode.CONDITION_TREE_IS_NOT_EMPTY, HttpStatus.BAD_REQUEST,
        "Дерево правила перехода не пусто. Для новых атомарных условий "
            + "необходимо указывать родительскую вершину");
  }
}

