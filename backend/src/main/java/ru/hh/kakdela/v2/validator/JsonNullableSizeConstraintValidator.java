package ru.hh.kakdela.v2.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.openapitools.jackson.nullable.JsonNullable;

public class JsonNullableSizeConstraintValidator
    implements ConstraintValidator<JsonNullableSize, JsonNullable<String>> {

  int min;
  int max;

  @Override
  public void initialize(JsonNullableSize constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
    min = constraintAnnotation.min();
    max = constraintAnnotation.max();
  }

  @Override
  public boolean isValid(JsonNullable<String> value, ConstraintValidatorContext context) {
    if (value == null || value.isUndefined()) {
      return true;
    }
    if (min < 0 || max < 0) {
      throw new IllegalArgumentException(
          "Значения параметров min и max должны быть неотрицательными");
    }
    String stringValue = value.get();
    if (stringValue != null) {
      return stringValue.length() >= min && stringValue.length() <= max;
    } else {
      return true;
    }
  }
}
