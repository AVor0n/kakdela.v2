package ru.hh.kakdela.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@Getter
@Schema(
    name = "ResponseCreateResponse",
    title = "DTO ответа при создании ответа на опрос"
)
public class ResponseCreateResponseDto {
  private final UUID id;
}
