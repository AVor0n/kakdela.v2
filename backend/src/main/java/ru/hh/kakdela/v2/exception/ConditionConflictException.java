package ru.hh.kakdela.v2.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class ConditionConflictException extends RuntimeException {

  private final UUID condition1Id;
  private final UUID condition2Id;
  private final String condition1Summary;
  private final String condition2Summary;
  private final String details;

  public ConditionConflictException(
      UUID condition1Id,
      UUID condition2Id,
      String condition1Summary,
      String condition2Summary,
      String details
  ) {
    super(String.format("Конфликт между условиями: '%s' и '%s'",
        condition1Summary, condition2Summary));
    this.condition1Id = condition1Id;
    this.condition2Id = condition2Id;
    this.condition1Summary = condition1Summary;
    this.condition2Summary = condition2Summary;
    this.details = details;
  }
}
