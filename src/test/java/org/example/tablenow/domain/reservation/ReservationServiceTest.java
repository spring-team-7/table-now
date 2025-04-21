package org.example.tablenow.domain.reservation;

import org.example.tablenow.domain.reservation.dto.request.ReservationRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationStatusChangeRequestDto;
import org.example.tablenow.domain.reservation.dto.request.ReservationUpdateRequestDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationResponseDto;
import org.example.tablenow.domain.reservation.dto.response.ReservationStatusResponseDto;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import org.example.tablenow.domain.reservation.repository.ReservationRepository;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StoreService storeService;

    @InjectMocks
    private ReservationService reservationService;

    Long userId = 1L;
    Long storeId = 10L;
    AuthUser authUser = new AuthUser(userId, "user@test.com", UserRole.ROLE_USER, "일반회원");
    User user = User.fromAuthUser(authUser);

    Store store = Store.builder()
            .id(storeId)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(22, 0))
            .capacity(20)
            .build();

    LocalDateTime reservedAt = LocalDateTime.of(2025, 4, 10, 10, 0);

    Reservation reserved1;
    Reservation reserved2;

    @BeforeEach
    void setUp() {
        reserved1 = createReservation(1L, LocalDateTime.of(2025, 4, 10, 10, 0), ReservationStatus.RESERVED);
        reserved2 = createReservation(2L, LocalDateTime.of(2025, 4, 11, 11, 30), ReservationStatus.RESERVED);
    }

    private Reservation createReservation(Long id, LocalDateTime time, ReservationStatus status) {
        Reservation reservation = Reservation.builder()
                .id(id)
                .user(user)
                .store(store)
                .reservedAt(time)
                .build();
        ReflectionTestUtils.setField(reservation, "status", status);
        return reservation;
    }

    @Nested
    class 락_기반_예약_생성 {

        ReservationRequestDto dto = ReservationRequestDto.builder()
                .storeId(storeId)
                .reservedAt(reservedAt)
                .build();

        @Test
        void 예약_성공() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(reservationRepository.countReservedTablesByDate(any(), any())).willReturn(0L);
            given(reservationRepository.existsByUserIdAndStoreIdAndReservedAt(anyLong(), anyLong(), any())).willReturn(false);
            given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ReservationResponseDto response = reservationService.makeReservationWithLock(authUser, dto);

            // then
            assertNotNull(response);
            assertEquals(reservedAt, response.getReservedAt());
            assertEquals(storeId, response.getStoreId());
        }

        @Test
        void 정원_초과시_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(reservationRepository.countReservedTablesByDate(any(), any())).willReturn(999L); // 정원 초과

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.makeReservationWithLock(authUser, dto)
            );

            assertEquals(ErrorCode.STORE_TABLE_CAPACITY_EXCEEDED.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 중복_예약시_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(reservationRepository.countReservedTablesByDate(any(), any())).willReturn(0L);
            given(reservationRepository.existsByUserIdAndStoreIdAndReservedAt(anyLong(), anyLong(), any())).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.makeReservationWithLock(authUser, dto)
            );

            assertEquals(ErrorCode.RESERVATION_DUPLICATE.getDefaultMessage(), exception.getMessage());
        }
    }

    @Nested
    class 예약_생성 {
        ReservationRequestDto dto = ReservationRequestDto.builder()
                .storeId(storeId)
                .reservedAt(reservedAt)
                .build();

        @Test
        void 이미_같은_유저의_예약이_존재하는_경우_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(reservationRepository.existsByUserIdAndStoreIdAndReservedAt(anyLong(), any(), any())).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.makeReservation(authUser, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.RESERVATION_DUPLICATE.getDefaultMessage());
        }

        @Test
        void 예약_시간이_가게_영업시간이_아니면_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(reservationRepository.existsByUserIdAndStoreIdAndReservedAt(anyLong(), any(), any())).willReturn(false);
            ReflectionTestUtils.setField(dto, "reservedAt", LocalDateTime.of(2025, 4, 10, 23, 0));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.makeReservation(authUser, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_CLOSED_TIME.getDefaultMessage());
        }

        @Test
        void 예약_성공() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(reservationRepository.existsByUserIdAndStoreIdAndReservedAt(anyLong(), any(), any())).willReturn(false);
            given(reservationRepository.save(any(Reservation.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ReservationResponseDto response = reservationService.makeReservation(authUser, dto);

            // then
            assertNotNull(response);
            assertEquals(response.getReservedAt(), reservedAt);
            assertEquals(response.getStoreId(), storeId);
        }
    }

    @Nested
    class 예약_날짜_수정 {
        Long reservationId = 1L;

        ReservationUpdateRequestDto dto = ReservationUpdateRequestDto.builder()
                .reservedAt(reservedAt)
                .build();

        @Test
        void 예약_당사자가_아닌_경우_예외_발생() {
            // given
            User otherUser = User.builder().id(999L).build();
            Reservation reservation = createReservation(reservationId, LocalDateTime.of(2025, 4, 10, 10, 0), ReservationStatus.RESERVED);
            ReflectionTestUtils.setField(reservation, "user", otherUser);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.updateReservation(authUser, reservationId, dto)
            );
            assertEquals(ErrorCode.RESERVATION_FORBIDDEN.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이미_예약이_존재하는_경우_예외_발생() {
            // given
            Reservation reservation = createReservation(reservationId, LocalDateTime.of(2025, 4, 10, 10, 0), ReservationStatus.RESERVED);
            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
            given(reservationRepository.existsByStoreIdAndReservedAtAndIdNot(anyLong(), any(), anyLong())).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.updateReservation(authUser, reservationId, dto)
            );
            assertEquals(ErrorCode.RESERVATION_DUPLICATE.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 수정_성공() {
            // given
            Reservation reservation = createReservation(reservationId, LocalDateTime.of(2025, 4, 10, 10, 0), ReservationStatus.RESERVED);
            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
            given(reservationRepository.existsByStoreIdAndReservedAtAndIdNot(anyLong(), any(), anyLong())).willReturn(false);

            // when
            ReservationResponseDto response = reservationService.updateReservation(authUser, reservationId, dto);

            // then
            assertNotNull(response);
            assertEquals(dto.getReservedAt(), response.getReservedAt());
        }
    }

    @Nested
    class 내_예약_목록_조회 {
        int page = 1;
        int size = 10;

        @Test
        void 예약된_목록_조회_성공() {
            // given
            ReservationStatus status = ReservationStatus.RESERVED;
            Page<Reservation> result = new PageImpl<>(List.of(reserved1, reserved2));
            given(reservationRepository.findByUserIdAndStatus(eq(userId), eq(status), any(Pageable.class)))
                    .willReturn(result);

            // when
            Page<ReservationResponseDto> response = reservationService.getReservations(authUser, status, page, size);

            // then
            assertNotNull(response);
            assertEquals(2, response.getTotalElements());

            ReservationResponseDto dto = response.getContent().get(0);
            assertAll(
                    () -> assertEquals(reserved1.getId(), dto.getReservationId()),
                    () -> assertEquals(storeId, dto.getStoreId()),
                    () -> assertEquals(reserved1.getReservedAt(), dto.getReservedAt()),
                    () -> assertEquals(reserved1.getStatus(), dto.getStatus())
            );
        }

        @Test
        void 취소된_목록_조회_성공() {
            // given
            ReflectionTestUtils.setField(reserved1, "status", ReservationStatus.CANCELED);
            ReflectionTestUtils.setField(reserved2, "status", ReservationStatus.CANCELED);

            ReservationStatus status = ReservationStatus.CANCELED;
            Page<Reservation> result = new PageImpl<>(List.of(reserved1, reserved2));

            given(reservationRepository.findByUserIdAndStatus(eq(userId), eq(status), any(Pageable.class)))
                    .willReturn(result);

            // when
            Page<ReservationResponseDto> response = reservationService.getReservations(authUser, status, page, size);

            // then
            assertNotNull(response);
            assertEquals(2, response.getTotalElements());
        }

        @Test
        void 완료된_목록_조회_성공() {
            // given
            ReflectionTestUtils.setField(reserved1, "status", ReservationStatus.COMPLETED);
            ReflectionTestUtils.setField(reserved2, "status", ReservationStatus.COMPLETED);

            ReservationStatus status = ReservationStatus.COMPLETED;
            Page<Reservation> result = new PageImpl<>(List.of(reserved1, reserved2));

            given(reservationRepository.findByUserIdAndStatus(eq(userId), eq(status), any(Pageable.class)))
                    .willReturn(result);

            // when
            Page<ReservationResponseDto> response = reservationService.getReservations(authUser, status, page, size);

            // then
            assertNotNull(response);
            assertEquals(2, response.getTotalElements());
        }
    }

    @Nested
    class 내_가게_예약_목록_조회 {
        int page = 1;
        int size = 10;

        @Test
        void 가게_주인이_아닌_경우_예외_발생() {
            // given
            given(storeService.getStore(eq(storeId))).willReturn(store);
            willThrow(new HandledException(ErrorCode.STORE_FORBIDDEN))
                    .given(storeService).validateStoreOwnerId(any(Store.class), any(User.class));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.getStoreReservations(authUser, storeId, ReservationStatus.RESERVED, page, size)
            );

            assertEquals(ErrorCode.STORE_FORBIDDEN.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 가게_예약_목록_조회_성공() {
            // given
            ReservationStatus status = ReservationStatus.RESERVED;
            Page<Reservation> result = new PageImpl<>(List.of(reserved1, reserved2));

            given(storeService.getStore(eq(storeId))).willReturn(store);
            willDoNothing().given(storeService).validateStoreOwnerId(any(Store.class), any(User.class));
            given(reservationRepository.findByStoreIdAndStatus(eq(storeId), eq(status), any(Pageable.class)))
                    .willReturn(result);

            // when
            Page<ReservationResponseDto> response = reservationService.getStoreReservations(authUser, storeId, status, page, size);

            // then
            assertNotNull(response);
            assertEquals(2, response.getTotalElements());

            ReservationResponseDto dto = response.getContent().get(0);
            assertAll(
                    () -> assertEquals(reserved1.getId(), dto.getReservationId()),
                    () -> assertEquals(reserved1.getReservedAt(), dto.getReservedAt()),
                    () -> assertEquals(reserved1.getStatus(), dto.getStatus())
            );
        }
    }

    @Nested
    class 빈자리_여부_조회 {

        LocalDate date = LocalDate.of(2025, 4, 10);

        @Test
        void 정원이_남아있으면_true_반환() {
            // given
            given(reservationRepository.countReservedTablesByDate(store, date)).willReturn(5L);

            // when
            boolean result = reservationService.hasVacancyDate(store, date);

            // then
            assertTrue(result);
        }

        @Test
        void 정원이_가득_찼으면_false_반환() {
            // given
            given(reservationRepository.countReservedTablesByDate(store, date)).willReturn(20L);

            // when
            boolean result = reservationService.hasVacancyDate(store, date);

            // then
            assertFalse(result);
        }

        @Test
        void 정원을_초과했으면_false_반환() {
            // given
            given(reservationRepository.countReservedTablesByDate(store, date)).willReturn(21L);

            // when
            boolean result = reservationService.hasVacancyDate(store, date);

            // then
            assertFalse(result);
        }
    }

    @Nested
    class 예약_취소 {

        @Test
        void 예약_당사자가_아닌_경우_예외_발생() {
            // given
            User otherUser = User.builder().id(999L).build();
            ReflectionTestUtils.setField(reserved1, "user", otherUser);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reserved1));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.cancelReservation(authUser, 100L)
            );
            assertEquals(ErrorCode.RESERVATION_FORBIDDEN.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 예약_취소_성공() {
            // given
            given(reservationRepository.findById(eq(1L))).willReturn(Optional.of(reserved1));

            // when
            ReservationStatusResponseDto response = reservationService.cancelReservation(authUser, 1L);

            // then
            assertNotNull(response);
            assertEquals(ReservationStatus.CANCELED, response.getStatus());
        }
    }

    @Nested
    class 예약_완료_처리 {

        @Test
        void 가게_주인이_아닌_경우_예외_발생() {
            // given
            AuthUser otherAuthUser = new AuthUser(999L, "x@test.com", UserRole.ROLE_USER, "다른유저");
            User otherUser = User.fromAuthUser(otherAuthUser);

            given(reservationRepository.findById(eq(1L))).willReturn(Optional.of(reserved1));
            willThrow(new HandledException(ErrorCode.STORE_FORBIDDEN))
                    .given(storeService).validateStoreOwnerId(eq(store), any(User.class));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.completeReservation(
                            otherAuthUser,
                            1L,
                            ReservationStatusChangeRequestDto.builder()
                                    .status(ReservationStatus.COMPLETED)
                                    .build()
                    )
            );

            assertEquals(ErrorCode.STORE_FORBIDDEN.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 예약_완료_처리_성공() {
            // given
            given(reservationRepository.findById(eq(1L))).willReturn(Optional.of(reserved1));
            willDoNothing().given(storeService).validateStoreOwnerId(eq(store), any(User.class));

            // when
            ReservationStatusResponseDto response = reservationService.completeReservation(
                    authUser,
                    1L,
                    ReservationStatusChangeRequestDto.builder()
                            .status(ReservationStatus.COMPLETED)
                            .build()
            );

            // then
            assertNotNull(response);
            assertEquals(ReservationStatus.COMPLETED, response.getStatus());
        }
    }

    @Nested
    class 리뷰작성_가능여부_검증 {

        Long userId = 1L;
        Long storeId = 10L;

        @Test
        void 리뷰_작성_가능한_예약이_존재하면_예외발생하지_않음() {
            // given
            given(reservationRepository.existsReviewableReservation(userId, storeId)).willReturn(true);

            // when & then
            assertDoesNotThrow(() -> reservationService.validateCreateRating(userId, storeId));
        }

        @Test
        void 리뷰_작성_가능한_예약이_없으면_예외발생() {
            // given
            given(reservationRepository.existsReviewableReservation(userId, storeId)).willReturn(false);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    reservationService.validateCreateRating(userId, storeId)
            );

            assertEquals(ErrorCode.RATING_RESERVATION_NOT_FOUND.getDefaultMessage(), exception.getMessage());
        }
    }
}
