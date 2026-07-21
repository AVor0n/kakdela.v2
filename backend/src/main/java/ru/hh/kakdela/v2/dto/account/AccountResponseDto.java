package ru.hh.kakdela.v2.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@Getter
@Schema(
    name = "AccountResponse",
    title = "DTO для получения данных аккаунта"
)
public class AccountResponseDto {

  private final UUID id;
  private final String login;
  private final String email;

}
