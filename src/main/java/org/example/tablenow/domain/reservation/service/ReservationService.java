package org.example.tablenow.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.example.tablenow.global.annotation.DistributedLock;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreService storeService;

    private static final String RESERVATION_LOCK_KEY_PREFIX = "lock:reservation";

    @Transactional
    public ReservationResponseDto makeReservation(AuthUser authUser, ReservationRequestDto request) {
        User user = User.fromAuthUser(authUser);
        Store store = storeService.getStore(request.getStoreId());

        validateStoreCapacity(store, request.getReservedAt());
        validateReservationDuplication(user, store, request.getReservedAt());
        validateStoreOpening(store, request.getReservedAt());

        Reservation reservation = Reservation.builder()
                .user(user)
                .store(store)
                .reservedAt(request.getReservedAt())
                .build();
        validateReservationOwner(reservation, user);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponseDto.fromReservation(savedReservation);
    }

    @DistributedLock(
            prefix = RESERVATION_LOCK_KEY_PREFIX,
            key = "#request.storeId + ':' + #request.reservedAt.toLocalDate()"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservationResponseDto makeReservationWithLock(AuthUser authUser, ReservationRequestDto request) {
        return handleReservationCreation(authUser, request);
    }

    private ReservationResponseDto handleReservationCreation(AuthUser authUser, ReservationRequestDto request) {
        User user = User.fromAuthUser(authUser);
        Store store = storeService.getStore(request.getStoreId());

        validateStoreCapacity(store, request.getReservedAt());
        validateReservationDuplication(user, store, request.getReservedAt());
        validateStoreOpening(store, request.getReservedAt());

        Reservation reservation = Reservation.builder()
                .user(user)
                .store(store)
                .reservedAt(request.getReservedAt())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponseDto.fromReservation(savedReservation);
    }

    @Transactional
    public ReservationResponseDto updateReservation(AuthUser authUser, Long id, ReservationUpdateRequestDto request) {
        User user = User.fromAuthUser(authUser);
        Reservation reservation = getReservation(id);

        validateUpdatableReservation(user, id, request, reservation);
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
        storeService.validateStoreOwnerId(store, user);

        Pageable pageable = PageRequest.of(page - 1, size);

        return reservationRepository.findByStoreIdAndStatus(store.getId(), status, pageable)
                .map(ReservationResponseDto::fromReservation);
    }

    @Transactional
    public ReservationStatusResponseDto completeReservation(AuthUser authUser, Long id, ReservationStatusChangeRequestDto request) {
        User user = User.fromAuthUser(authUser);
        Reservation reservation = getReservation(id);
        storeService.validateStoreOwnerId(reservation.getStore(), user);

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

    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateUpdatableReservation(User user, Long id, ReservationUpdateRequestDto request, Reservation reservation) {
        validateReservationOwner(reservation, user);
        validateReservationTimeDuplicated(id, request, reservation);
    }

    private void validateReservationOwner(Reservation reservation, User user) {
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new HandledException(ErrorCode.RESERVATION_FORBIDDEN);
        }
    }

    private void validateReservationTimeDuplicated(Long id, ReservationUpdateRequestDto request, Reservation reservation) {
        boolean isDuplicated = reservationRepository.existsByStoreIdAndReservedAtAndIdNot(
                reservation.getStore().getId(),
                request.getReservedAt(),
                id
        );

        if (isDuplicated) {
            throw new HandledException(ErrorCode.RESERVATION_DUPLICATE);
        }
    }

    private void validateStoreCapacity(Store store, LocalDateTime reservedAt) {
        LocalDate date = reservedAt.toLocalDate();
        long reservedCount = reservationRepository.countReservedTablesByDate(store, date);

        if (reservedCount >= store.getCapacity()) {
            throw new HandledException(ErrorCode.STORE_TABLE_CAPACITY_EXCEEDED);
        }
    }

    private void validateReservationDuplication(User user, Store store, LocalDateTime reservedAt) {
        boolean exists = reservationRepository.existsByUserIdAndStoreIdAndReservedAt(
                user.getId(), store.getId(), reservedAt
        );

        if (exists) {
            throw new HandledException(ErrorCode.RESERVATION_DUPLICATE);
        }
    }

    private void validateStoreOpening(Store store, LocalDateTime reservedAt) {
        if (!store.isOpenAt(reservedAt)) {
            throw new HandledException(ErrorCode.STORE_CLOSED_TIME);
        }
    }

    // TODO: 예약 취소 시 빈자리 이벤트 발생 시킬 예정 (추후 RabbitMQ 발행으로 구조 전환)
    public boolean hasVacancy(Store store) {
        long reservedCount = reservationRepository.countReservedTables(store);
        return store.hasVacancy(reservedCount);
    }

    public void validateCreateRating(Long userId, Long storeId) {
        if (!reservationRepository.existsReviewableReservation(userId, storeId)) {
            throw new HandledException(ErrorCode.RATING_RESERVATION_NOT_FOUND);
        }
    }

    public boolean hasVacancyDate(Store store, LocalDate date){
        long reservedCount = reservationRepository.countReservedTablesByDate(store, date);
        return store.hasVacancy(reservedCount);
    }
}