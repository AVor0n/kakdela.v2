package ru.hh.kakdela.v2.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class ConditionConflictException extends RuntimeException {

  private final UUID condition1Id;
  private final UUID condition2Id;

  public ConditionConflictException(UUID conditionId1, UUID conditionId2) {
    super("CONDITION_CONFLICT");
    this.condition1Id = conditionId1;
    this.condition2Id = conditionId2;
  }
}
