package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class ConditionNodeNotFoundException extends Kd2ObjectRelatedException {
  public ConditionNodeNotFoundException(UUID id) {
    super(ErrorCode.CONDITION_NODE_NOT_FOUND, HttpStatus.NOT_FOUND,
        "Вершина правила перехода не найдена", id, null);
  }
}
