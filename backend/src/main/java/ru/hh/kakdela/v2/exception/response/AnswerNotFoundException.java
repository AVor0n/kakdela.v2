package ru.hh.kakdela.v2.exception.response;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class AnswerNotFoundException extends Kd2ObjectRelatedException {
  public AnswerNotFoundException(UUID responseId, UUID questionId) {
    super(ErrorCode.ANSWER_NOT_FOUND, HttpStatus.NOT_FOUND,
        "Ответ не найден", responseId, questionId, null);
  }
}
