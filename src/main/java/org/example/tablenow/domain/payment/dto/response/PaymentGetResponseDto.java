package org.example.tablenow.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentGetResponseDto {

    private final long paymentId;
    private final long reservationId;
    private final long userId;
    private final String paymentNumber;
    private final String method;
    private final int price;
    private final PaymentStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static PaymentGetResponseDto from(Payment payment) {
        return new PaymentGetResponseDto(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getUser().getId(),
                payment.getPaymentNumber(),
                payment.getMethod(),
                payment.getPrice(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}