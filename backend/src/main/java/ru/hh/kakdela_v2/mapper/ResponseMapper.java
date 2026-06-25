package ru.hh.kakdela_v2.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hh.kakdela_v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela_v2.model.Response;

public class ResponseMapper {

  public static ResponseResponseDto responseToDto(Response response) {
    return new ResponseResponseDto(
        response.getId(),
        response.getAccount() != null
            ? response.getAccount().getId()
            : null,
        response.getSurvey().getId(),
        response.isCompleted(),
        response.getReceivedAt(),
        response.getAnswers().stream()
            .map(AnswerMapper::answerToDto)
            .toList()
    );
  }
}
