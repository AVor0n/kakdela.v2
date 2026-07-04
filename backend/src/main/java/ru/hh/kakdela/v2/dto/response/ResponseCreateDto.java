package ru.hh.kakdela.v2.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "ResponseCreate",
    title = "DTO для создания ответа на опрос"
)
public class ResponseCreateDto {

  // account_id не передаём — берётся из токена авторизации в сервисе
  // survey_id передаем — к какому опросу относится ответ
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "ID опроса обязателен")
  private UUID surveyId;
}
