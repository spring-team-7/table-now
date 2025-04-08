package org.example.tablenow.domain.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.reservation.dto.request.ReservationRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationStatusChangeRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationUpdateRequestDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationStatusResponseDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationResponseDto;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/v1/reservations")
    public ResponseEntity<ReservationResponseDto> makeReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ReservationRequestDto request
    ) {
        return ResponseEntity.ok(reservationService.makeReservation(authUser, request));
    }

    @PatchMapping("/v1/reservations/{id}")
    public ResponseEntity<ReservationResponseDto> updateReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequestDto request
    ) {
        return ResponseEntity.ok(reservationService.updateReservation(authUser, id, request));
    }

    @GetMapping("/v1/reservations")
    public ResponseEntity<Page<ReservationResponseDto>> getReservations(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) ReservationStatus status,
            @Positive @RequestParam(defaultValue = "1") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reservationService.getReservations(authUser, status, page, size));
    }

    @Secured("ROLE_OWNER")
    @GetMapping("/v1/owner/stores/{storeId}/reservations")
    public ResponseEntity<Page<ReservationResponseDto>> getStoreReservations(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestParam(required = false) ReservationStatus status,
            @Positive @RequestParam(defaultValue = "1") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reservationService.getStoreReservations(authUser, storeId, status, page, size));
    }

    @Secured("ROLE_OWNER")
    @PatchMapping("/v1/owner/reservations/{id}")
    public ResponseEntity<ReservationStatusResponseDto> completeReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusChangeRequestDto request
    ) {
        return ResponseEntity.ok(reservationService.completeReservation(authUser, id, request));
    }

    @DeleteMapping("/v1/reservations/{id}")
    public ResponseEntity<ReservationStatusResponseDto> cancelReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok((reservationService.cancelReservation(authUser, id)));
    }
}
