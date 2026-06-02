package ru.hh.kakdela_v2.dto;

import java.util.UUID;

public class AccountTokenDto {

  private String token;
  private UUID accountId;

  public AccountTokenDto(String token, UUID accountId) {
    this.token = token;
    this.accountId = accountId;
  }

  public String getToken() { return token; }
  public UUID getAccountId() { return accountId; }
}