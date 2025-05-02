package org.example.tablenow.domain.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.reservation.dto.request.ReservationRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationSearchRequest;
import org.example.tablenow.domain.reservation.dto.request.ReservationStatusChangeRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationUpdateRequestDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationResponseDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationStatusResponseDto;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "예약 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "예약 생성")
    @PostMapping("/v1/reservations")
    public ResponseEntity<ReservationResponseDto> makeReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ReservationRequestDto request
    ) {
        return ResponseEntity.ok(reservationService.makeReservation(authUser, request));
    }

    @Operation(summary = "예약 생성 (Redisson Lock)")
    @PostMapping("/v2/reservations")
    public ResponseEntity<ReservationResponseDto> makeReservationWithLock(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ReservationRequestDto request
    ) {
        return ResponseEntity.ok(reservationService.makeReservationWithLock(authUser, request));
    }

    @Operation(summary = "예약 수정")
    @PatchMapping("/v1/reservations/{id}")
    public ResponseEntity<ReservationResponseDto> updateReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequestDto request
    ) {
        return ResponseEntity.ok(reservationService.updateReservation(authUser, id, request));
    }

    @Operation(summary = "내 예약 목록 조회")
    @GetMapping("/v1/reservations")
    public ResponseEntity<Page<ReservationResponseDto>> getReservations(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute ReservationSearchRequest request
    ) {
        return ResponseEntity.ok(
                reservationService.getReservations(authUser, request.getStatus(), request.getPage(), request.getSize())
        );
    }

    @Operation(summary = "사장님 예약 목록 조회")
    @Secured(UserRole.Authority.OWNER)
    @GetMapping("/v1/owner/stores/{storeId}/reservations")
    public ResponseEntity<Page<ReservationResponseDto>> getStoreReservations(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @ModelAttribute ReservationSearchRequest request
    ) {
        return ResponseEntity.ok(reservationService.getStoreReservations(authUser, storeId, request.getStatus(), request.getPage(), request.getSize()));
    }

    @Operation(summary = "예약 확정/완료 처리 (사장님)")
    @Secured(UserRole.Authority.OWNER)
    @PatchMapping("/v1/owner/reservations/{id}")
    public ResponseEntity<ReservationStatusResponseDto> completeReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusChangeRequestDto request
    ) {
        return ResponseEntity.ok(reservationService.completeReservation(authUser, id, request));
    }

    @Operation(summary = "예약 취소")
    @DeleteMapping("/v1/reservations/{id}")
    public ResponseEntity<ReservationStatusResponseDto> cancelReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok((reservationService.cancelReservation(authUser, id)));
    }
}
