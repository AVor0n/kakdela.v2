package ru.hh.kakdela_v2.dto.answer_option;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AnswerOptionUpdateDto {

  @NotNull(message = "Порядковый номер обязателен")
  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;
  @NotBlank(message = "Текст варианта ответа не должен быть пустым")
  @Max(value = 1000, message = "Текст варианта ответа не должен быть длиннее 1000 символов")
  private String answerOptionText;
}
