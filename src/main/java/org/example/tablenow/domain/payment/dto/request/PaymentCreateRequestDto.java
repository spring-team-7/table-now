package org.example.tablenow.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PaymentCreateRequestDto {

    @NotBlank
    private String method;

    @NotNull
    private int price;
}
