package ru.yandex.practicum.filmorate.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.annotations.DateAfter;

import java.time.LocalDate;

public class DateAfterValidator implements ConstraintValidator<DateAfter, LocalDate> {

    private LocalDate minDate;

    @Override
    public void initialize(DateAfter constraintAnnotation) {
        this.minDate = LocalDate.parse(constraintAnnotation.date());
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if (localDate == null) {
            return true;
        }
        return !localDate.isBefore(minDate);
    }

}
