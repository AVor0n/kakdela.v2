package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2OblectRelatedException;

@Getter
public class ConditionConflictException extends Kd2OblectRelatedException {

  public ConditionConflictException(UUID condition1Id, UUID condition2Id) {
    super(
        ErrorCode.CONDITIONS_OF_PAGE_HAVE_CONFLICTS,
        HttpStatus.CONFLICT,
        "Конфликт в условиях: condition1Id=%s, condition2Id=%s"
            .formatted(condition1Id, condition2Id),
        condition1Id,
        condition2Id);
  }
}
