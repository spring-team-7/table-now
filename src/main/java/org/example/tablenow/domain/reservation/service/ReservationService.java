package org.example.tablenow.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.reservation.dto.request.ReservationRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationUpdateRequestDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationDeleteResponseDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationResponseDto;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import org.example.tablenow.domain.reservation.repository.ReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationResponseDto makeReservation(Long userId, ReservationRequestDto request) {
        // TODO: 로그인한 유저인지 확인하는 로직 추가

        if (reservationRepository.existsByStoreIdAndReservedAt(request.getStoreId(), request.getReservedAt())) {
            throw new IllegalArgumentException("해당 시간에는 이미 예약이 존재합니다.");
        }

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .storeId(request.getStoreId())
                .reservedAt(request.getReservedAt())
                .build();

        reservationRepository.save(reservation);

        return toResponseDto(reservation);
    }

    @Transactional
    public ReservationResponseDto updateReservation(Long userId, Long id, ReservationUpdateRequestDto request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));

        // TODO: 해당 예약을 한 유저와 같은 유저인지 확인하는 로직 추가

        validateReservationUpdateDuplication(id, request, reservation);

        reservation.updateReservedAt(request.getReservedAt());
        Reservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();

        return toResponseDto(updated);
    }

    private void validateReservationUpdateDuplication(Long id, ReservationUpdateRequestDto request, Reservation reservation) {
        // TODO: 취소된 예약은 수정할 수 없도록 예외 추가

        if (reservationRepository.existsByStoreIdAndReservedAtAndIdNot(
                reservation.getStoreId(),
                request.getReservedAt(),
                id)
        ) {
            throw new IllegalArgumentException("해당 날짜 및 시간에는 이미 예약이 존재합니다.");
        }
    }

    public Page<ReservationResponseDto> getReservations(Long userId, ReservationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return reservationRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(this::toResponseDto);
    }

    @Transactional
    public ReservationDeleteResponseDto deleteReservation(Long userId, Long id) {
        // TODO: 해당 예약을 한 유저와 같은 유저인지 확인하는 로직 추가
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));
        reservation.cancel();
        return ReservationDeleteResponseDto.builder()
                .reservationId(reservation.getId())
                .modifiedAt(reservation.getUpdatedAt())
                .status(reservation.getStatus())
                .build();
    }

    private ReservationResponseDto toResponseDto(Reservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getId())
                .storeId(reservation.getStoreId())
                .reservedAt(reservation.getReservedAt())
                .createdAt(reservation.getCreatedAt())
                .modifiedAt(reservation.getUpdatedAt())
                .status(reservation.getStatus())
                .build();
    }
}