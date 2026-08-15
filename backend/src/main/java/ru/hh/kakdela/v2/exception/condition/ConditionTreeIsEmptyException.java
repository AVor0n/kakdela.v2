package ru.hh.kakdela.v2.exception.condition;

import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;

public class ConditionTreeIsEmptyException extends Kd2Exception {
  public ConditionTreeIsEmptyException() {
    super(ErrorCode.CONDITION_TREE_IS_EMPTY, HttpStatus.BAD_REQUEST,
        "Дерево правила перехода пусто. Добавление соединительных вершин не допускается, "
            + "а у атомарных условий не может быть родительской вершины");
  }
}
