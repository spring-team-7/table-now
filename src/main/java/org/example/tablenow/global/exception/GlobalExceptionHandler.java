package org.example.tablenow.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
