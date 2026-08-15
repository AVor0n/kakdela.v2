package ru.hh.kakdela.v2.exception.security;

import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2AuthenticationException;

public class WrongPasswordException extends Kd2AuthenticationException {
  public WrongPasswordException(String login) {
    super(ErrorCode.WRONG_PASSWORD, "Неверный пароль", login);
  }
}
