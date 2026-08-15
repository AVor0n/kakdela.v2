package ru.hh.kakdela.v2.exception.survey.page;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2OblectRelatedException;

public class SurveyPageNotFoundException extends Kd2OblectRelatedException {
  public SurveyPageNotFoundException(UUID id) {
    super(ErrorCode.SURVEY_PAGE_NOT_FOUND, HttpStatus.NOT_FOUND,
        "Страница не найдена", id, null);
  }
}
