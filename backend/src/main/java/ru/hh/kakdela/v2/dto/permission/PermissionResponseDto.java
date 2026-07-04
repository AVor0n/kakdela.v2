package ru.hh.kakdela.v2.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@Getter
@Schema(
    name = "PermissionResponse",
    title = "DTO для получения данных роли"
)
public class PermissionResponseDto {

  private final UUID accountId;
  private final UUID surveyId;
  private final String role;
  private final Boolean doNotify;
}
