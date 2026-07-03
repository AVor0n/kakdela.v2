package ru.hh.kakdela.v2.dto.answer_option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "AnswerOptionCreate",
    title = "DTO для создания варианта ответа"
)
public class AnswerOptionCreateDto {

  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;
  @NotBlank(message = "Текст варианта ответа не должен быть пустым")
  @Size(max = 1000, message = "Текст варианта ответа не должен быть длиннее 1000 символов")
  private String answerOptionText;
}
