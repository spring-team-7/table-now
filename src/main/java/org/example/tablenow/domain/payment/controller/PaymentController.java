package org.example.tablenow.domain.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.dto.request.PaymentCreateRequestDto;
import org.example.tablenow.domain.payment.dto.response.PaymentResponseDto;
import org.example.tablenow.domain.payment.service.PaymentService;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/v1/reservations/{reservationId}/payments")
    public ResponseEntity<PaymentResponseDto> confirmPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId,
            @RequestBody @Valid PaymentCreateRequestDto paymentCreateRequestDto
    ) {
        return ResponseEntity.ok(paymentService.confirmPayment(authUser, reservationId, paymentCreateRequestDto));
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/v1/reservations/{reservationId}/payments/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId,
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(paymentService.getPayment(authUser, reservationId, paymentId));
    }

    @Secured(UserRole.Authority.USER)
    @PatchMapping("/v1/reservations/{reservationId}/payments/{paymentId}")
    public ResponseEntity<PaymentResponseDto> cancelPayment (
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId,
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(paymentService.cancelPayment(authUser, reservationId, paymentId));
    }
}
