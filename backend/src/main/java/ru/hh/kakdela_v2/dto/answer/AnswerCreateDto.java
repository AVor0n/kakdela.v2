package ru.hh.kakdela_v2.dto.answer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerCreateDto {

  @NotNull(message = "ID вопроса обязателен")
  private UUID questionId;

  @NotBlank(message = "Текст ответа не может быть пустым")
  private String answerText;
}
