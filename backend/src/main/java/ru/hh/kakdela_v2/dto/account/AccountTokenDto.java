package ru.hh.kakdela_v2.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AccountTokenDto {

  private String token;
  private UUID accountId;
}
