package ru.hh.kakdela.v2.exception.security;

import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2AuthenticationException;

public class AccountDeletedException extends Kd2AuthenticationException {

  public AccountDeletedException(String login) {
    super(ErrorCode.ACCOUNT_DELETED,
        "Аккаунт удалён", login);
  }
}
