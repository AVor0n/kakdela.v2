package ru.hh.kakdela.v2.validator;

import jakarta.validation.Payload;

public @interface JsonNullableUndefinedOrNotNullAndNotBlank {
  String message() default "Строка не должна быть null и не должна быть пустой";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
