package org.example.tablenow.domain.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.dto.request.PaymentCreateRequestDto;
import org.example.tablenow.domain.payment.dto.response.PaymentCancelResponseDto;
import org.example.tablenow.domain.payment.dto.response.PaymentCreateResponseDto;
import org.example.tablenow.domain.payment.dto.response.PaymentGetResponseDto;
import org.example.tablenow.domain.payment.service.PaymentService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/v1/reservations/{reservationId}/payments")
    public ResponseEntity<PaymentCreateResponseDto> confirmPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId,
            @RequestBody @Valid PaymentCreateRequestDto paymentCreateRequestDto
    ) {
        return ResponseEntity.ok(paymentService.confirmPayment(authUser, reservationId, paymentCreateRequestDto));
    }

    @GetMapping("/v1/reservations/{reservationId}/payments/{paymentId}")
    public ResponseEntity<PaymentGetResponseDto> getPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId,
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(paymentService.getPayment(authUser, reservationId, paymentId));
    }

    @DeleteMapping("/v1/reservations/{reservationId}/payments/{paymentId}")
    public ResponseEntity<PaymentCancelResponseDto> cancelPayment (
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId,
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(paymentService.cancelPayment(authUser, reservationId, paymentId));
    }
}