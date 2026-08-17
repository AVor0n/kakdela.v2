package ru.hh.kakdela.v2.exception.question;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class AnswerOptionNotFoundException extends Kd2ObjectRelatedException {
  public AnswerOptionNotFoundException(UUID id) {
    super(ErrorCode.ANSWER_OPTION_NOT_FOUND, HttpStatus.NOT_FOUND,
        "Вариант ответа не найден", id, null, null);
  }
}
