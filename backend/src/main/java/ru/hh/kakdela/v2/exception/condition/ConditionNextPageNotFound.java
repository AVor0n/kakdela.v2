package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class ConditionNextPageNotFound extends Kd2ObjectRelatedException {
  public ConditionNextPageNotFound(UUID id) {
    super(ErrorCode.CONDITION_NEXT_PAGE_NOT_FOUND, HttpStatus.NOT_FOUND,
        "Указанная страница перехода не найдена", id, null);
  }
}
