package ru.hh.kakdela.v2.dto.condition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.DefaultValues;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Condition.Create")
public class ConditionRequestDto {

  @NotNull
  private UUID nextPageId;
  @NotNull
  private Boolean isActive = DefaultValues.CONDITION_IS_ACTIVE;
}
