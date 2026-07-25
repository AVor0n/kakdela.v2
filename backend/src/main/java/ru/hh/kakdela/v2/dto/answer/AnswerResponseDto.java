package ru.hh.kakdela.v2.dto.answer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(
    name = "AnswerResponse",
    title = "DTO для получения данных ответа на вопрос"
)
public class AnswerResponseDto {

  private final UUID responseId;
  private final UUID questionId;
  private final String answerText;
}
