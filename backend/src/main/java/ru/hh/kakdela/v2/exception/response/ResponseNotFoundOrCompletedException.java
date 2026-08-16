package ru.hh.kakdela.v2.exception.response;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class ResponseNotFoundOrCompletedException extends Kd2ObjectRelatedException {
  public ResponseNotFoundOrCompletedException(UUID id) {
    super(ErrorCode.RESPONSE_NOT_FOUND_OR_COMPLETED, HttpStatus.BAD_REQUEST,
        "Прохождение не найдено или уже завершено", id, null, null);
  }
}
