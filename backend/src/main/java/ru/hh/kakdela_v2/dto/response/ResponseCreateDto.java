package ru.hh.kakdela_v2.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class ResponseCreateDto {

  // account_id не передаём — берётся из токена авторизации в сервисе
  // survey_id передаем — к какому опросу относится ответ
  @NotNull(message = "ID опроса обязателен")
  private UUID surveyId;
}
