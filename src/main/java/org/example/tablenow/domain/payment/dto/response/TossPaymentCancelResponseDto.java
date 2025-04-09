package org.example.tablenow.domain.payment.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossPaymentCancelResponseDto {
    private String paymentKey;
    private String orderId;
    private String status;
    private String canceledAt;
}
