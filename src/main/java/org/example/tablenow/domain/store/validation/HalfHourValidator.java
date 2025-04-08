package org.example.tablenow.domain.store.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.tablenow.domain.store.validation.annotation.HalfHourOnly;

import java.time.LocalTime;

public class HalfHourValidator implements ConstraintValidator<HalfHourOnly, LocalTime> {

    @Override
    public boolean isValid(LocalTime time, ConstraintValidatorContext context) {
        if (time == null) return true;

        int minute = time.getMinute();
        return minute == 0 || minute == 30;
    }
}
