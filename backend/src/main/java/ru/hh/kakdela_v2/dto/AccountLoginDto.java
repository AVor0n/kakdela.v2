package ru.hh.kakdela_v2.dto;

public class AccountLoginDto {
  private String login;
  private String rawPassword;

  public AccountLoginDto() {
  }

  public String getLogin() {
    return login;
  }

  public String getRawPassword() {
    return rawPassword;
  }
}