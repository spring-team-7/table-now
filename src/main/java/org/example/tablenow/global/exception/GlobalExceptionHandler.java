package org.example.tablenow.global.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        FieldError firstError = ex.getBindingResult().getFieldError();
        String message = "유효성 검사에 실패했습니다.";

        if (firstError != null) {
            message = firstError.getDefaultMessage();
        }

        return new ResponseEntity<>(ErrorResponse.of(httpStatus, message), httpStatus);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            String fieldName = invalidFormatException.getPath().stream()
                    .findFirst()
                    .map(JsonMappingException.Reference::getFieldName)
                    .orElse("알 수 없는 필드");

            String invalidValue = String.valueOf(invalidFormatException.getValue());
            String errorMessage = String.format("필드 '%s'에 대한 값 '%s'이(가) 올바르지 않습니다.", fieldName, invalidValue);

            return new ResponseEntity<>(ErrorResponse.of(httpStatus, errorMessage), httpStatus);
        }
        return new ResponseEntity<>(ErrorResponse.of(httpStatus, "잘못된 요청입니다."), httpStatus);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        return new ResponseEntity<>(ErrorResponse.of(httpStatus, ex.getMessage()), httpStatus);
    }

    @ExceptionHandler(HandledException.class)
    public ResponseEntity<ErrorResponse<String>> invalidRequestExceptionException(HandledException ex) {
        HttpStatus httpStatus = ex.getHttpStatus();
        return new ResponseEntity<>(ErrorResponse.of(httpStatus, ex.getMessage()), ex.getHttpStatus());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse<String> handleGlobalException(Exception e) {
        log.error("Exception : {}",e.getMessage(),  e);
        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }
}
