package ru.hh.kakdela.v2.dto.answer.option;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Schema(
    name = "AnswerOptionResponse",
    title = "DTO для получения данных варианта ответа"
)
public class AnswerOptionResponseDto {

  private final UUID id;
  private final int serialNumber;
  private final String text;
  private final String attachmentUrl;
}
