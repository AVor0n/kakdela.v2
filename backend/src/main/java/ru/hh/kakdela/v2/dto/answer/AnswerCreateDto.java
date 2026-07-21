package ru.hh.kakdela.v2.dto.answer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import io.swagger.v3.oas.annotations.media.Schema;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "AnswerCreate",
    title = "DTO для создания ответа на вопрос"
)
public class AnswerCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Текст ответа не должен быть пустым")
  private String answerText;
}
