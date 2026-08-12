package ru.hh.kakdela.v2.dto.condition;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(name = "Condition.NextPageResponse")
public class ConditionNextPageResponseDto {

  private final UUID nextPageId;
}
