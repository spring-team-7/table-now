package org.example.tablenow.domain.payment.enums;

public enum PaymentStatus {
    READY("결제 대기"),      // 결제 대기
    DONE("결제 완료"),  // 결제 취소
    CANCELED("결제 취소");

    private final String description;

    PaymentStatus(String description){
        this.description = description;
    }
}
