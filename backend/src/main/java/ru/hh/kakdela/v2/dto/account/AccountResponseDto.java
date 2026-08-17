package ru.hh.kakdela.v2.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Schema(
    name = "AccountResponse",
    title = "DTO для получения данных аккаунта"
)
public class AccountResponseDto {

  private final UUID id;
  private final String login;
  private final String email;
  private final Boolean hhSso;

}
