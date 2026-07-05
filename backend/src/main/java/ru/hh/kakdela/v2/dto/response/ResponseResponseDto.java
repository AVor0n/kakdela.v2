package ru.hh.kakdela.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.dto.answer.AnswerResponseDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@Getter
@Schema(
    name = " ResponseResponse",
    title = "DTO для получения данных ответа на опрос"
)
public class ResponseResponseDto {

  private final UUID id;
  private final UUID accountId;   // может быть null — анонимное прохождение
  private final UUID surveyId;
  private final Boolean isCompleted;
  private final Instant receivedAt;
  private final List<AnswerResponseDto> answers;
}
