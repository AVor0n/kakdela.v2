package ru.hh.kakdela.v2.status;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ObjectStatus {
  CREATED(HttpStatus.CREATED),
  UPDATED(HttpStatus.OK);

  public final HttpStatus httpStatus;
}
