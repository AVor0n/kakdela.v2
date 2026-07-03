package ru.hh.kakdela.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@Getter
@Schema(
    name = "ResponseWithTokenDto",
    title = "DTO ответа на опрос с токеном доступа"
)
public class ResponseWithTokenDto {
  private final UUID id;
  private final String responseAccessToken;
}
