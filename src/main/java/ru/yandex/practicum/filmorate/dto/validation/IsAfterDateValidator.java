package ru.yandex.practicum.filmorate.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class IsAfterDateValidator implements ConstraintValidator<After, LocalDate> {
    private LocalDate threshold;
    private boolean inclusive;

    @Override
    public void initialize(After constraintAnnotation) {
        threshold = LocalDate.parse(constraintAnnotation.value());
        inclusive = constraintAnnotation.inclusive();
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if (localDate == null) {
            return true;
        }
        return inclusive ? !localDate.isBefore(threshold) : localDate.isAfter(threshold);
    }
}
