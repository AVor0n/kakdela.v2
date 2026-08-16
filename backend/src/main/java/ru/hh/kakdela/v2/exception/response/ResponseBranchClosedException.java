package ru.hh.kakdela.v2.exception.response;

import org.springframework.http.HttpStatus;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;

public class ResponseBranchClosedException extends Kd2Exception {
  public ResponseBranchClosedException() {
    super(ErrorCode.RESPONSE_BRANCH_CLOSED, HttpStatus.FORBIDDEN,
        "Ветка с данной страницей закрыта правилами перехода");
  }
}
