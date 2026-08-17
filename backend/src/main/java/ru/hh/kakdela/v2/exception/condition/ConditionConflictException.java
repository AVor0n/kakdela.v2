package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class ConditionConflictException extends Kd2ObjectRelatedException {
  public ConditionConflictException(UUID condition1Id, UUID condition2Id) {
    super(
        ErrorCode.CONDITIONS_OF_PAGE_HAVE_CONFLICTS, HttpStatus.CONFLICT,
        "Конфликт правил перехода", condition1Id, condition2Id, null);
  }
}
