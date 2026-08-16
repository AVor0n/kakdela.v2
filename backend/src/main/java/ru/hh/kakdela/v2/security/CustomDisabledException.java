package ru.hh.kakdela.v2.security;

import lombok.Getter;
import org.springframework.security.authentication.DisabledException;

@Getter
public class CustomDisabledException extends DisabledException {

  private static final String DEFAULT_USER_DELETED_MESSAGE = "user deleted";

  private final String name;

  private CustomDisabledException(String name) {
    super(DEFAULT_USER_DELETED_MESSAGE);
    this.name = name;
  }

  public static CustomDisabledException fromUsername(String username) {
    return new CustomDisabledException(username);
  }
}
