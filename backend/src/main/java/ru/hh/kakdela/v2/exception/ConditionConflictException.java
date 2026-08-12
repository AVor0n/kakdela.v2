package ru.hh.kakdela.v2.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ConditionConflictException extends Kd2OblectRelatedException {

  public ConditionConflictException(UUID conditionId1, UUID conditionId2) {
    super(
      ErrorCode.CONDITIONS_OF_PAGE_HAVE_CONFLICTS, 
      HttpStatus.CONFLICT,
      "Конфликт в условиях",
      conditionId1, 
      conditionId2);
  }
}
