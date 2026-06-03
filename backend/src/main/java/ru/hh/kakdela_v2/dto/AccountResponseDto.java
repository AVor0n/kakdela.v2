package ru.hh.kakdela_v2.dto;

import lombok.Getter;
import ru.hh.kakdela_v2.model.Account;

import java.time.Instant;
import java.util.UUID;

@Getter
public class AccountResponseDto {

  private final UUID id;
  private final String login;
  private final String email;
  private final Instant registeredAt;

  public AccountResponseDto(Account account) {
    this.id = account.getId();
    this.login = account.getLogin();
    this.email = account.getEmail();
    this.registeredAt = account.getRegisteredAt();
  }
}