package ru.hh.kakdela.v2.dto.condition.atom;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "ConditionAtom.Create")
public class ConditionAtomCreateDto extends ConditionAtomUpdateDto {
  private UUID parentNodeId;
}
