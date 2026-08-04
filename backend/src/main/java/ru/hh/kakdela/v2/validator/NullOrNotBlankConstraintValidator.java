package ru.hh.kakdela.v2.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullOrNotBlankConstraintValidator
    implements ConstraintValidator<NullOrNotBlank, CharSequence> {
  @Override
  public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return !value.toString().trim().isEmpty();
  }
}
