package ru.hh.kakdela.v2.exception.response;

import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;

public class NotAllMandatoryQuestionsAnsweredException extends Kd2Exception {

  public NotAllMandatoryQuestionsAnsweredException() {
    super(ErrorCode.NOT_ALL_MANDATORY_QUESTIONS_ANSWERED,
        HttpStatus.CONFLICT,
        "Не все обязательные вопросы заполнены");
  }
}
