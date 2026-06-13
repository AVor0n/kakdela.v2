package ru.hh.kakdela_v2.dto.account;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountCreateDto {

  private String login;
  private String email;
  private String hashPassword;
}
