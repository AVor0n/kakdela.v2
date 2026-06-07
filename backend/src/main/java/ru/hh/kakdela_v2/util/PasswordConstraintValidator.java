package ru.hh.kakdela_v2.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    return password.matches("^(?=.*[0-9])(?=.*[a-zA-Zа-яА-Я])(?=.*[!@#&()–\\[{}\\]:;',?/*~$^+=<>]).{8,}$");
  }
}
