package ru.hh.kakdela.v2.dto.answer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "AnswerUpdate",
    title = "DTO для обновления ответа на вопрос"
)
public class AnswerUpdateDto {

  @NullOrNotBlank(message = "Текст ответа не должен быть пустым")
  private String answerText;
}
