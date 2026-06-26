package ru.hh.kakdela.v2.dto.account;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountUpdateDto {

  private String login;
  private String email;
  private String hashPassword;
}
