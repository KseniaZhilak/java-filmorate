package ru.yandex.practicum.filmorate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validations.DateAfterValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateAfterValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateAfter {

    String message() default "Дата должна быть не раньше 28 декабря 1895 года";

    String date();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
