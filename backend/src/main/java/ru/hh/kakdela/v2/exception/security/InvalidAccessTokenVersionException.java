package ru.hh.kakdela.v2.exception.security;

import ru.hh.kakdela.v2.exception.Kd2AuthenticationException;

public class InvalidAccessTokenVersionException extends Kd2AuthenticationException {
  public InvalidAccessTokenVersionException() {
    super("Версия предоставленного access token устарела", null);
  }
}
