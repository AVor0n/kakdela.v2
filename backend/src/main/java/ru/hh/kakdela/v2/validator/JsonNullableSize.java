package ru.hh.kakdela.v2.validator;

import jakarta.validation.Payload;

public @interface JsonNullableSize {
  String message() default "Строка не соответствует разрешённым размерам";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
  int min() default 0;
  int max() default Integer.MAX_VALUE;
}
