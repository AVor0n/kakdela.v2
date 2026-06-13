package ru.hh.kakdela_v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela_v2.model.Response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class ResponseResponseDto {

  private final UUID responseId;
  private final UUID accountId;   // может быть null — анонимное прохождение
  private final UUID surveyId;
  private final boolean isComplete;
  private final Instant receivedAt;
  private final List<AnswerResponseDto> answers;

  public ResponseResponseDto(Response response) {
    this.responseId = response.getId();
    this.accountId = response.getAccount() != null
        ? response.getAccount().getId()
        : null;
    this.surveyId = response.getSurvey().getId();
    this.isComplete = response.isComplete();
    this.receivedAt = response.getReceivedAt();
    this.answers = response.getAnswers().stream()
        .map(AnswerResponseDto::new)
        .toList();
  }
}
