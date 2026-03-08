package ru.yandex.practicum.filmorate.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IsAfterDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface After {
    String message() default "Date must be after {value}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String value();

    boolean inclusive() default true;
}
