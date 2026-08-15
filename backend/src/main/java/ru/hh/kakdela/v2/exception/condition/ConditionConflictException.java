package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2OblectRelatedException;

public class ConditionConflictException extends Kd2OblectRelatedException {

  public ConditionConflictException(UUID condition1Id, UUID condition2Id) {
    super(
        ErrorCode.CONDITIONS_OF_PAGE_HAVE_CONFLICTS, HttpStatus.CONFLICT,
        "Конфликт правил перехода", condition1Id, condition2Id);
  }
}
