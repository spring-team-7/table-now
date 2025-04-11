package org.example.tablenow.domain.payment.enums;

import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;

public enum PaymentStatus {
    READY("결제 대기"),
    DONE("결제 완료"),
    CANCELED("결제 취소");

    private final String description;

    PaymentStatus(String description){
        this.description = description;
    }

    public static PaymentStatus from(String status, ErrorCode errorCode) {
        try {
            return PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new HandledException(errorCode);
        }
    }
}
