package ru.hh.kakdela.v2.dto.condition.atom;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(name = "ConditionAtom.Response")
public class ConditionAtomResponseDto {

  private final UUID questionId;
  private final Boolean requiredBooleanValue;
  private final UUID requiredAnswerOptionId;
  private final String operator;
}
