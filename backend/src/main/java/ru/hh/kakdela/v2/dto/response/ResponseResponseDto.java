package ru.hh.kakdela.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.dto.answer.AnswerResponseDto;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Schema(name = " Response.Response")
public class ResponseResponseDto {

  private final UUID id;
  private final AccountResponseDto account;   // может быть null — анонимное прохождение
  private final UUID surveyId;
  private final Boolean isCompleted;
  private final Instant receivedAt;
  private final List<AnswerResponseDto> answers;
}
