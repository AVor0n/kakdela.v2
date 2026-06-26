package ru.hh.kakdela.v2.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NullOrNotBlankConstraintValidator.class)
public @interface NullOrNotBlank {
  String message() default "Строка не должна быть пустой";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}