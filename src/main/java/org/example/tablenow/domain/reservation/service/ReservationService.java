package org.example.tablenow.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.reservation.dto.request.ReservationRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationStatusChangeRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationUpdateRequestDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationResponseDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationStatusResponseDto;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import org.example.tablenow.domain.reservation.repository.ReservationRepository;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreService storeService;

    @Transactional
    public ReservationResponseDto makeReservation(AuthUser authUser, ReservationRequestDto request) {
        User user = User.fromAuthUser(authUser);
        Store store = storeService.getStore(request.getStoreId());

        if (reservationRepository.isReservedStatusInUse(request.getStoreId(), request.getReservedAt())) {
            throw new HandledException(ErrorCode.RESERVATION_DUPLICATE);
        }

        // TODO: 가게 오픈 및 마감시간 검증 필요

        Reservation reservation = Reservation.builder()
                .user(user)
                .store(store)
                .reservedAt(request.getReservedAt())
                .build();
        validateReservationOwner(reservation, user);

        reservationRepository.save(reservation);

        return ReservationResponseDto.fromReservation(reservation);
    }

    @Transactional
    public ReservationResponseDto updateReservation(AuthUser authUser, Long id, ReservationUpdateRequestDto request) {
        User user = User.fromAuthUser(authUser);
        Reservation reservation = getReservation(id);

        validateReservationOwner(reservation, user);
        validateReservationUpdateDuplication(id, request, reservation);
        reservation.updateReservedAt(request.getReservedAt());

        return ReservationResponseDto.fromReservation(reservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> getReservations(AuthUser authUser, ReservationStatus status, int page, int size) {
        User user = User.fromAuthUser(authUser);
        Pageable pageable = PageRequest.of(page - 1, size);

        return reservationRepository.findByUserIdAndStatus(user.getId(), status, pageable)
                .map(ReservationResponseDto::fromReservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> getStoreReservations(AuthUser authUser, Long storeId, ReservationStatus status, int page, int size) {
        User user = User.fromAuthUser(authUser);
        Store store = storeService.getStore(storeId);
        storeService.validStoreOwnerId(store, user);

        Pageable pageable = PageRequest.of(page - 1, size);

        return reservationRepository.findByStoreIdAndStatus(store.getId(), status, pageable)
                .map(ReservationResponseDto::fromReservation);
    }

    @Transactional
    public ReservationStatusResponseDto completeReservation(AuthUser authUser, Long id, ReservationStatusChangeRequestDto request) {
        User user = User.fromAuthUser(authUser);
        Reservation reservation = getReservation(id);
        storeService.validStoreOwnerId(reservation.getStore(), user);

        reservation.updateStatus(request.getStatus());

        return ReservationStatusResponseDto.fromReservation(reservation);
    }

    @Transactional
    public ReservationStatusResponseDto cancelReservation(AuthUser authUser, Long id) {
        User user = User.fromAuthUser(authUser);
        Reservation reservation = getReservation(id);
        validateReservationOwner(reservation, user);

        reservation.tryCancel();

        return ReservationStatusResponseDto.fromReservation(reservation);
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private static void validateReservationOwner(Reservation reservation, User user) {
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new HandledException(ErrorCode.RESERVATION_FORBIDDEN);
        }
    }

    private void validateReservationUpdateDuplication(Long id, ReservationUpdateRequestDto request, Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new HandledException(ErrorCode.RESERVATION_ALREADY_CANCELED);
        }

        boolean isDuplicated = reservationRepository.existsByStoreIdAndReservedAtAndIdNot(
                reservation.getStore().getId(),
                request.getReservedAt(),
                id
        );

        if (isDuplicated) {
            throw new HandledException(ErrorCode.RESERVATION_DUPLICATE);
        }
    }
}