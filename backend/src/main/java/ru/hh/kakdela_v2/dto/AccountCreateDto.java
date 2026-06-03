package ru.hh.kakdela_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateDto {

  private String login;
  private String email;
  private String rawPassword;
  private String rawPasswordConfirmation;
}