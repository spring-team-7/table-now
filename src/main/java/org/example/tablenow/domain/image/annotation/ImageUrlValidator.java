package org.example.tablenow.domain.image.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.MalformedURLException;
import java.net.URL;

public class ImageUrlValidator implements ConstraintValidator<ImageUrlPattern, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;  // null 또는 빈 문자열 허용
        }

        try {
            new URL(value);  // java.net.URL을 사용해 유효성 검사
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
