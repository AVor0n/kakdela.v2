package ru.hh.kakdela.v2.dto.condition.node;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.model.condition.ConditionNode;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "ConditionNode.Update")
public class ConditionNodeUpdateDto {

  @NotNull
  private ConditionNode.Operator operator;
}

