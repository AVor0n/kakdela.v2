package ru.hh.kakdela.v2.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordConstraintValidator.class)
public @interface ValidPassword {
  String message() default "Пароль должен иметь длину не менее 8 символов и состоять из букв, цифр и специальных символов";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}