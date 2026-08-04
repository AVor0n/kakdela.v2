package ru.hh.kakdela.v2.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.openapitools.jackson.nullable.JsonNullable;

public class JsonNullableUndefinedOrNotNullAndNotBlankConstraintValidator
    implements
        ConstraintValidator<JsonNullableUndefinedOrNotNullAndNotBlank, JsonNullable<String>> {
  @Override
  public boolean isValid(JsonNullable<String> value, ConstraintValidatorContext context) {
    if (value == null || value.isUndefined()) {
      return true;
    }
    String stringValue = value.get();
    return stringValue != null && !stringValue.trim().isEmpty();
  }
}
