package ru.hh.kakdela.v2.mapper;

import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.model.Account;

public class AccountMapper {

  public static AccountResponseDto accountToDto(Account account) {
    return new AccountResponseDto(
        account.getId(),
        account.getLogin(),
        account.getEmail(),
        account.getRegisteredAt()
    );
  }
}
