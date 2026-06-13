package ru.hh.kakdela_v2.dto.response;

import lombok.Getter;
import lombok.Value;
import ru.hh.kakdela_v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela_v2.model.Response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
public class ResponseResponseDto {

  UUID responseId;
  UUID accountId;   // может быть null — анонимное прохождение
  UUID surveyId;
  boolean isComplete;
  Instant receivedAt;
  List<AnswerResponseDto> answers;

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
