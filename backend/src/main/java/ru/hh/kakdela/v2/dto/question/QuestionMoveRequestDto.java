package ru.hh.kakdela.v2.dto.question;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class QuestionMoveRequestDto {

  @NotNull(message = "Новая позиция обязательна")
  @Min(value = 1, message = "Позиция должна быть больше 0")
  private Integer newPosition;
}
