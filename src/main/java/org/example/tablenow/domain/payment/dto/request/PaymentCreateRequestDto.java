package org.example.tablenow.domain.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequestDto {
    private String paymentKey;
    private String orderId; // 예약 ID
    private int amount;
}
