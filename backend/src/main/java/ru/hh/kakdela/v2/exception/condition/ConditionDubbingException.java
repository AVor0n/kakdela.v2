package ru.hh.kakdela.v2.exception.condition;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2OblectRelatedException;

public class ConditionDubbingException extends Kd2OblectRelatedException {
  public ConditionDubbingException(UUID dubbedId) {
    super(ErrorCode.CONDITION_FOR_THESE_PAGE_AND_NEXT_PAGE_ALREADY_EXISTS,
        HttpStatus.CONFLICT,
        "Для данной страницы уже существует правило с указанной страницей перехода",
        dubbedId,
        null);
  }
}
