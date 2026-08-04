package ru.hh.kakdela.v2.dto.answer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.status.ObjectStatus;

@AllArgsConstructor
@Getter
public class AnswerResponseDtoWithStatusDto {

  AnswerResponseDto answer;
  ObjectStatus status;
}
