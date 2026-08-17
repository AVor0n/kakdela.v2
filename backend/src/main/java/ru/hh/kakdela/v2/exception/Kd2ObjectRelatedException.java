package ru.hh.kakdela.v2.exception;

import java.util.UUID;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class Kd2ObjectRelatedException extends Kd2Exception {
  private final UUID object1Id;
  private final UUID object2Id;
  private final String objectDetails;

  public Kd2ObjectRelatedException(
      ErrorCode errorCode,
      HttpStatus httpStatus,
      String message,
      UUID object1Id,
      UUID object2Id,
      String objectDetails
  ) {
    super(errorCode, httpStatus, message);
    this.object1Id = object1Id;
    this.object2Id = object2Id;
    this.objectDetails = objectDetails;
  }
}
