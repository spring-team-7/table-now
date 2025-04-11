package org.example.tablenow.domain.reservation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class HalfHourValidator implements ConstraintValidator<HalfHourOnly, LocalDateTime> {
    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) return true;
        int minute = value.getMinute();
        return minute == 0 || minute == 30;
    }
}
