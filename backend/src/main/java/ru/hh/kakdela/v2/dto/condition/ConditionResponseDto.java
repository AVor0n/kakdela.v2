package ru.hh.kakdela.v2.dto.condition;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeResponseDto;

@AllArgsConstructor
@Getter
@Schema(name = "Condition.Response")
public class ConditionResponseDto {

  private final UUID id;
  private final UUID pageId;
  private final UUID nextPageId;
  private final Boolean isActive;
  private final ConditionNodeResponseDto root;
}
