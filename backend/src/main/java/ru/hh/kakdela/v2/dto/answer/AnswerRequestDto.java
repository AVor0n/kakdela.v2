package ru.hh.kakdela.v2.dto.answer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "Answer.Request"
)
public class AnswerRequestDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = "Текст ответа не должен быть пустым")
  @Size(max = 5000, message = "Текст ответа не должен быть длиннее 5000 символов")
  private String textValue;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Boolean booleanValue;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private LocalDate dateValue;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private LocalTime timeValue;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Set<UUID> selectedAnswerOptionIds;
}
