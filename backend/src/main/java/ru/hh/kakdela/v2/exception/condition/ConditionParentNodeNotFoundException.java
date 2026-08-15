package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2OblectRelatedException;

public class ConditionParentNodeNotFoundException extends Kd2OblectRelatedException {
  public ConditionParentNodeNotFoundException(UUID id) {
    super(ErrorCode.CONDITION_CHILD_NODE_NOT_FOUND, HttpStatus.NOT_FOUND,
        "Указанная родительская вершина не найдена", id, null);
  }
}