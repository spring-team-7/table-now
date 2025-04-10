package org.example.tablenow.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class TossPaymentConfirmResponseDto {
    private final String paymentKey;
    private final String orderId;
    private final String method;
    private final int totalAmount;
    private final String status;
}
