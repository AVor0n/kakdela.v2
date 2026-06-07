package ru.hh.kakdela_v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCreateDto {

  // account_id не передаём — берётся из токена авторизации в сервисе
  // survey_id передаем — к какому опросу относится ответ
  private UUID surveyId;
}
