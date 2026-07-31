package ru.hh.kakdela.v2.dto.condition.atom;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.model.condition.ConditionAtom;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "ConditionAtom.Update")
public class ConditionAtomUpdateDto {
  @NotNull
  private UUID questionId;
  private Boolean requiredBooleanValue;
  private UUID requiredAnswerOptionId;
  @NotNull
  private ConditionAtom.Operator operator;
}
