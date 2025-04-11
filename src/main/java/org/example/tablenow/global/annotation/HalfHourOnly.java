package org.example.tablenow.global.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {
        HalfHourValidatorForLocalTime.class,
        HalfHourValidatorForLocalDateTime.class
})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HalfHourOnly {
    String message() default "시간은 00분 또는 30분만 허용됩니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
