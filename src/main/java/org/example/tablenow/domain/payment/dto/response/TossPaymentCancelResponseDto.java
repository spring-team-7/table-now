package org.example.tablenow.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class TossPaymentCancelResponseDto {
    private final String paymentKey;
    private final String orderId;
    private final String status;
    private final String canceledAt;
}
