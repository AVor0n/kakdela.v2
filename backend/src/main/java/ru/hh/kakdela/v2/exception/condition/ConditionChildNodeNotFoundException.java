package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class ConditionChildNodeNotFoundException extends Kd2ObjectRelatedException {
  public ConditionChildNodeNotFoundException(UUID id) {
    super(ErrorCode.CONDITION_CHILD_NODE_NOT_FOUND, HttpStatus.NOT_FOUND,
        "Указанная дочерняя вершина не найдена", id, null);
  }
}
