package ru.hh.kakdela_v2.dto.answer;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela_v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
public class AnswerUpdateDto {

  @NullOrNotBlank(message = "Текст ответа не должен быть пустым")
  @Size(max = 5000, message = "Текст ответа не должен быть длиннее 5000 символов")
  private String answerText;
}
