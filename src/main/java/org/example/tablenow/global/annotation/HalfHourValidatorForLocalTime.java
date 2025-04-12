package org.example.tablenow.global.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;

public class HalfHourValidatorForLocalTime implements ConstraintValidator<HalfHourOnly, LocalTime> {

    @Override
    public boolean isValid(LocalTime time, ConstraintValidatorContext context) {
        if (time == null) return true;

        int minute = time.getMinute();
        return minute == 0 || minute == 30;
    }
}
