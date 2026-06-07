package ru.hh.kakdela_v2.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateDto {

  private String login;
  private String email;
  private String hashPassword;
}