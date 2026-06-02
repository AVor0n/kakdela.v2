package ru.hh.kakdela_v2.dto;

public class AccountCreateDto {

  private String login;
  private String email;
  private String rawPassword;

  public AccountCreateDto() {
  }

  public AccountCreateDto(String login, String email, String rawPassword) {
    this.login = login;
    this.email = email;
    this.rawPassword = rawPassword;
  }

  public String getLogin() {
    return login;
  }

  public String getEmail() {
    return email;
  }

  public String getRawPassword() {
    return rawPassword;
  }
}