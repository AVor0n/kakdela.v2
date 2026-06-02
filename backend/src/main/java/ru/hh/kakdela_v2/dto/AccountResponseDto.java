package ru.hh.kakdela_v2.dto;

import java.time.Instant;
import java.util.UUID;

public class AccountResponseDto {

  private UUID id;
  private String login;
  private String email;
  private Instant registeredAt;

  public AccountResponseDto(Account account) {
    this.id = account.getId();
    this.login = account.getLogin();
    this.email = account.getEmail();
    this.registeredAt = account.getRegisteredAt();
  }

  public UUID getId() {
    return id;
  }

  public String getLogin() {
    return login;
  }

  public String getEmail() {
    return email;
  }

  public Instant getRegisteredAt() {
    return registeredAt;
  }
}