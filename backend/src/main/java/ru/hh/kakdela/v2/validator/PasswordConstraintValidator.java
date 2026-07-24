package ru.hh.kakdela.v2.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator
    implements ConstraintValidator<ValidPassword, CharSequence> {
  @Override
  public boolean isValid(CharSequence password, ConstraintValidatorContext context) {
    return password.toString()
        .matches("^(?=.*[0-9])(?=.*[a-zA-Zа-яА-Я])(?=.*[!@#&()–\\[{}\\]:;',?/*~$^+=<>]).{8,}$");
  }
}
