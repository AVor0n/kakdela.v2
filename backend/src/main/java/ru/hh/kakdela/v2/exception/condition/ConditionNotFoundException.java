package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class ConditionNotFoundException extends Kd2ObjectRelatedException {
  public ConditionNotFoundException(UUID id) {
    super(ErrorCode.CONDITION_NOT_FOUND, HttpStatus.NOT_FOUND,
        "Правило перехода не найдено", id, null);
  }
}
