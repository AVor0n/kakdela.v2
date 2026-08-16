package ru.hh.kakdela.v2.exception.survey;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class SurveyIsEmptyException extends Kd2ObjectRelatedException {
  public SurveyIsEmptyException(UUID id) {
    super(ErrorCode.SURVEY_IS_EMPTY, HttpStatus.CONFLICT,
        "Опрос пуст", id, null, null);
  }
}
