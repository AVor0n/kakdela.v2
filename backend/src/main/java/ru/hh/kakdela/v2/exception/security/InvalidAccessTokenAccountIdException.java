package ru.hh.kakdela.v2.exception.security;

import ru.hh.kakdela.v2.exception.Kd2AuthenticationException;

public class InvalidAccessTokenAccountIdException extends Kd2AuthenticationException {
  public InvalidAccessTokenAccountIdException() {
    super("Предоставленный access token имеет неверный ID аккаунта", null);
  }
}
