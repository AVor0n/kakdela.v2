package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class ConditionNodeIsNotAtomException extends Kd2ObjectRelatedException {
  public ConditionNodeIsNotAtomException(UUID id) {
    super(ErrorCode.CONDITION_NODE_IS_NOT_ATOM, HttpStatus.BAD_REQUEST,
        "Указанная вершина не является атомарным условием", id, null);
  }
}
