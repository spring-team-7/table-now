package org.example.tablenow.global.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse<T> {

    private String status;

    private Integer code;

    private T message;

    public ErrorResponse(HttpStatus httpStatus, T message) {
        this.status = httpStatus.name();
        this.code = httpStatus.value();
        this.message = message;
    }

    public static <T> ErrorResponse<T> of(HttpStatus httpStatus, T message) {
        return new ErrorResponse<>(httpStatus, message);
    }
}
