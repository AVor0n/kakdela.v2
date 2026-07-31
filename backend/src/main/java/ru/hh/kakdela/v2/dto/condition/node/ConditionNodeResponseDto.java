package ru.hh.kakdela.v2.dto.condition.node;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.dto.condition.atom.ConditionAtomResponseDto;

@AllArgsConstructor
@Getter
@Schema(name = "ConditionNode.Response")
public class ConditionNodeResponseDto {

  private final UUID id;
  private final List<ConditionNodeResponseDto> children;
  private final String operator;
  private final ConditionAtomResponseDto atom;
}
