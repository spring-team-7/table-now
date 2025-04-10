package org.example.tablenow.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.dto.request.PaymentCreateRequestDto;
import org.example.tablenow.domain.payment.dto.response.TossPaymentCancelResponseDto;
import org.example.tablenow.domain.payment.dto.response.TossPaymentConfirmResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    @Value("${toss.secret.key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public TossPaymentConfirmResponseDto confirmPayment(PaymentCreateRequestDto paymentCreateRequestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String authHeader = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + authHeader);

        HttpEntity<PaymentCreateRequestDto> entity = new HttpEntity<>(paymentCreateRequestDto, headers);

        ResponseEntity<TossPaymentConfirmResponseDto> response = restTemplate.exchange(
                "https://api.tosspayments.com/v1/payments/confirm",
                HttpMethod.POST,
                entity,
                TossPaymentConfirmResponseDto.class
        );

        return response.getBody();
    }

    public TossPaymentCancelResponseDto cancelPayment(String paymentKey, String cancelReason) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String authHeader = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + authHeader);

        Map<String, String> body = new HashMap<>();
        body.put("cancelReason", cancelReason);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<TossPaymentCancelResponseDto> response = restTemplate.exchange(
                "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel",
                HttpMethod.POST,
                entity,
                TossPaymentCancelResponseDto.class
        );

        return response.getBody();
    }
}
