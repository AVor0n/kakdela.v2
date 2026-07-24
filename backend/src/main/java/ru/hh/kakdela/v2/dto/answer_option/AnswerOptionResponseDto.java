package ru.hh.kakdela.v2.dto.answer_option;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(
    name = "AnswerOptionResponse",
    title = "DTO для получения данных варианта ответа"
)
public class AnswerOptionResponseDto {

  private final UUID id;
  private final int serialNumber;
  private final String answerOptionText;
  private final String attachmentUrl;
}
