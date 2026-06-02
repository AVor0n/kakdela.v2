package ru.hh.kakdela_v2.dto;

public class AccountUpdateDto {

  private String login;
  private String email;

  public AccountUpdateDto() {
  }

  public String getLogin() {
    return login;
  }

  public String getEmail() {
    return email;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}