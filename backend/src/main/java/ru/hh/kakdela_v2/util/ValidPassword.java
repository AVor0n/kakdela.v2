package ru.hh.kakdela_v2.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordConstraintValidator.class)
public @interface ValidPassword {
  String message() default "The password must be at least 8 characters long and include numbers, letters, and special characters";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}