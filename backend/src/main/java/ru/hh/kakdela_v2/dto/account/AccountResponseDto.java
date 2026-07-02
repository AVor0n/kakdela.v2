package ru.hh.kakdela_v2.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.model.Account;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class AccountResponseDto {

  private final UUID id;
  private final String login;
  private final String email;
  private final Instant registeredAt;

}
