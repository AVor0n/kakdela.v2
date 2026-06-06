package ru.hh.kakdela_v2.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountUpdateDto {

  private String login;
  private String email;
  private String hashPassword;
}