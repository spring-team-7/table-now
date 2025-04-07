package org.example.tablenow.domain.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.reservation.dto.request.ReservationRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationUpdateRequestDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationDeleteResponseDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationResponseDto;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
            @Valid @RequestBody ReservationRequestDto request
    ) {
        // TODO: 추후 @Auth로 변경
        Long userId = 1L; // 임시 아이디
        return ResponseEntity.ok(reservationService.makeReservation(userId, request));
    }

    @PatchMapping("/v1/reservations/{id}")
    public ResponseEntity<ReservationResponseDto> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequestDto request
    ) {
        // TODO: 추후 @Auth로 변경
        Long userId = 1L; // 임시 아이디
        return ResponseEntity.ok(reservationService.updateReservation(userId, id, request));
    }

    @GetMapping("/v1/reservations")
    public ResponseEntity<Page<ReservationResponseDto>> getReservations(
            @RequestParam(required = false) ReservationStatus status,
            @Positive @RequestParam(defaultValue = "1") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        // TODO: 추후 @Auth로 변경
        Long userId = 1L; // 임시 아이디
        return ResponseEntity.ok(reservationService.getReservations(userId, status, page, size));
    }

    @DeleteMapping("/v1/reservations/{id}")
    public ResponseEntity<ReservationDeleteResponseDto> deleteReservation(
            @PathVariable Long id
    ) {
        // TODO: 추후 @Auth로 변경
        Long userId = 1L; // 임시 아이디
        return ResponseEntity.ok((reservationService.deleteReservation(userId, id)));
    }
}
