package ru.hh.kakdela.v2.exception.question;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2OblectRelatedException;

public class QuestionNotFoundException extends Kd2OblectRelatedException {
  public QuestionNotFoundException(UUID id) {
    super(ErrorCode.QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND,
        "Вопрос не найден", id, null);
  }
}
