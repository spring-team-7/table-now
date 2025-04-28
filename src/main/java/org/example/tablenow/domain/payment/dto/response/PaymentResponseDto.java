package org.example.tablenow.domain.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;

import java.time.LocalDateTime;

@Getter
public class PaymentResponseDto {

    private final long paymentId;
    private final long reservationId;
    private final long userId;
    private final String paymentKey;
    private final String method;
    private final int price;
    private final PaymentStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public PaymentResponseDto(long paymentId, long reservationId, long userId, String paymentKey, String method, int price, PaymentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.userId = userId;
        this.paymentKey = paymentKey;
        this.method = method;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public static PaymentResponseDto fromPayment(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .reservationId(payment.getReservationId())
                .userId(payment.getUserId())
                .paymentKey(payment.getPaymentKey())
                .method(payment.getMethod())
                .price(payment.getPrice())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
