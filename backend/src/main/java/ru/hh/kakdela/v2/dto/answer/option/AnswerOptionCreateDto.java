package ru.hh.kakdela.v2.dto.answer.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "AnswerOption.Create"
)
public class AnswerOptionCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Текст варианта ответа не должен быть пустым")
  private String text;
}
