package org.example.tablenow.domain.payment.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossPaymentConfirmResponseDto {
    private String paymentKey;
    private String orderId;
    private String method;
    private int totalAmount;
    private String status;
}
