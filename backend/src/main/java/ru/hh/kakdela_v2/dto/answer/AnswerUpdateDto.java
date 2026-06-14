package ru.hh.kakdela_v2.dto.answer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class AnswerUpdateDto {

  @NotNull(message = "ID вопроса обязателен")
  private UUID questionId;
  @NotBlank(message = "Текст ответа не должен быть пустым")
  @Max(value = 5000, message = "Текст ответа не должен быть длиннее 5000 символов")
  private String answerText;
}
