package ru.hh.kakdela.v2.mapper;

import ru.hh.kakdela.v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela.v2.model.Response;

public class ResponseMapper {

  public static ResponseResponseDto responseToDto(Response response) {
    return new ResponseResponseDto(
        response.getId(),
        response.getAccount() != null
            ? AccountMapper.accountToDto(response.getAccount())
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
