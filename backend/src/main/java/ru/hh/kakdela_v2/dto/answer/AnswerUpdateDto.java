package ru.hh.kakdela_v2.dto.answer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerUpdateDto {

  @NotBlank(message = "Текст ответа не может быть пустым")
  private String answerText;
}
