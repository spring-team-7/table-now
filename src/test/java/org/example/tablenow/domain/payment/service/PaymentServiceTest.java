package org.example.tablenow.domain.payment.service;

import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.payment.dto.request.PaymentCreateRequestDto;
import org.example.tablenow.domain.payment.dto.response.PaymentResponseDto;
import org.example.tablenow.domain.payment.dto.response.TossPaymentCancelResponseDto;
import org.example.tablenow.domain.payment.dto.response.TossPaymentConfirmResponseDto;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;
import org.example.tablenow.domain.payment.repository.PaymentRepository;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.repository.ReservationRepository;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private TossPaymentClient tossPaymentClient;
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private PaymentService paymentService;

    Long userId = 1L;
    Long ownerId = 2L;
    AuthUser authUser = new AuthUser(userId, "user@a.com", UserRole.ROLE_USER, "일반회원");
    User user = User.fromAuthUser(authUser);
    AuthUser authOwner = new AuthUser(ownerId, "owner@a.com", UserRole.ROLE_OWNER, "사장");
    User owner = User.fromAuthUser(authOwner);

    Long categoryId = 1L;
    Category category = Category.builder().id(categoryId).name("한식").build();

    Long storeId = 1L;
    Store store = Store.builder()
            .id(storeId)
            .name("한식 팔아요")
            .description("한식 먹고 가세요")
            .address("대구광역시 북구")
            .imageUrl(null)
            .capacity(10)
            .deposit(30000)
            .startTime(LocalTime.of(11,00))
            .endTime(LocalTime.of(20,00))
            .user(owner)
            .category(category)
            .build();

    Long reservationId = 1L;
    Reservation reservation = Reservation.builder()
            .id(reservationId)
            .user(user)
            .store(store)
            .reservedAt(LocalDateTime.of(2025, 4, 13, 12, 0, 0))
            .build();

    Long paymentId = 1L;
    Payment payment = Payment.builder()
            .id(paymentId)
            .paymentKey("1234qwerasdfzxcv")
            .method("Card")
            .price(20000)
            .status(PaymentStatus.READY)
            .user(user)
            .reservation(reservation)
            .build();

    @Nested
    class 결제_요청 {

        PaymentCreateRequestDto dto = new PaymentCreateRequestDto();

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(dto, "paymentKey", "tgen_20250410150357N34V7");
            ReflectionTestUtils.setField(dto, "orderId", "reservation-" + reservationId);
            ReflectionTestUtils.setField(dto, "amount", store.getDeposit());
        }

        @Test
        void 로그인_한_유저와_예약한_유저가_다른_경우_예외_발생() {
            // given
            AuthUser otherUser = new AuthUser(99L, "other@user.com", UserRole.ROLE_USER, "다른회원");
            given(reservationService.getReservation(anyLong())).willReturn(reservation);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.confirmPayment(otherUser, reservationId, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS.getDefaultMessage());
        }

        @Test
        void 이미_결제된_경우_예외_발생() {
            // given
            given(reservationService.getReservation(anyLong())).willReturn(reservation);
            given(paymentRepository.existsByReservationId(reservationId)).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.confirmPayment(authUser, reservationId, dto)
            );

            assertEquals(ErrorCode.ALREADY_PAID.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void Client에서_전달받은_금액과_가게의_예약금이_다른_경우_예외_발생() {
            // given
            given(reservationService.getReservation(anyLong())).willReturn(reservation);
            given(paymentRepository.existsByReservationId(reservationId)).willReturn(false);

            ReflectionTestUtils.setField(dto, "amount", 25000);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.confirmPayment(authUser, reservationId, dto)
            );

            assertEquals(ErrorCode.PAYMENT_AMOUNT_MISMATCH.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void tosspayment에서_결제가_완료되지_않은_경우_예외_발생() {
            // given
            given(reservationService.getReservation(anyLong())).willReturn(reservation);
            given(paymentRepository.existsByReservationId(reservationId)).willReturn(false);

            TossPaymentConfirmResponseDto tossResponse = TossPaymentConfirmResponseDto.builder()
                    .paymentKey(dto.getPaymentKey())
                    .orderId(dto.getOrderId())
                    .status("FAILED")
                    .method("CARD")
                    .totalAmount(dto.getAmount())
                    .build();

            given(tossPaymentClient.confirmPayment(dto)).willReturn(tossResponse);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.confirmPayment(authUser, reservationId, dto)
            );

            assertEquals(ErrorCode.TOSS_PAYMENT_FAILED.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 결제_완료() {
            // given
            given(reservationService.getReservation(anyLong())).willReturn(reservation);
            given(paymentRepository.existsByReservationId(reservationId)).willReturn(false);

            TossPaymentConfirmResponseDto tossResponse = TossPaymentConfirmResponseDto.builder()
                    .paymentKey(dto.getPaymentKey())
                    .orderId(dto.getOrderId())
                    .status("DONE")
                    .method("CARD")
                    .totalAmount(dto.getAmount())
                    .build();

            given(tossPaymentClient.confirmPayment(dto)).willReturn(tossResponse);

            given(paymentRepository.save(any(Payment.class)))
                    .willAnswer(invocation -> {
                        Payment savedPayment = invocation.getArgument(0);
                        ReflectionTestUtils.setField(savedPayment, "id", paymentId);
                        return savedPayment;
                    });

            // when
            PaymentResponseDto response = paymentService.confirmPayment(authUser, reservationId, dto);

            // then
            assertEquals(paymentId, response.getPaymentId());
            assertEquals(reservationId, response.getReservationId());
            assertEquals(authUser.getId(), response.getUserId());
            assertEquals("CARD", response.getMethod());
            assertEquals(dto.getAmount(), response.getPrice());
            assertEquals(PaymentStatus.DONE, response.getStatus());
        }
    }

    @Nested
    class 결제_조회 {

        @Test
        void 결제_내역이_없는_경우_예외_발생() {
            // given
            given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.getPayment(authUser, reservationId, paymentId)
            );

            assertEquals(ErrorCode.PAYMENT_NOT_FOUND.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 다른_유저가_내_결제에_접근하려는_경우_예외_발생() {
            // given
            AuthUser otherAuthUser = new AuthUser(99L, "other@a.com", UserRole.ROLE_USER, "다른회원");

            Reservation myReservation = Reservation.builder()
                    .id(reservationId)
                    .user(user)
                    .store(store)
                    .reservedAt(LocalDateTime.of(2025, 4, 13, 12, 0))
                    .build();

            Payment myPayment = Payment.builder()
                    .id(paymentId)
                    .paymentKey("tgen_20250410150357N34V7")
                    .method("CARD")
                    .price(30000)
                    .status(PaymentStatus.DONE)
                    .user(user)
                    .reservation(myReservation)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(myPayment));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.getPayment(otherAuthUser, reservationId, paymentId)
            );

            assertEquals(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS.getDefaultMessage(), exception.getMessage());
        }


        @Test
        void 결제_내역의_예약ID와_입력받은_예약ID가_다른_경우_예외_발생() {
            // given
            Reservation validReservation = Reservation.builder()
                    .id(reservationId) // reservationId = 1L
                    .user(user)
                    .store(store)
                    .reservedAt(LocalDateTime.of(2025, 4, 13, 12, 0))
                    .build();

            Payment payment = Payment.builder()
                    .id(paymentId)
                    .paymentKey("tgen_20250410150357N34V7")
                    .method("CARD")
                    .price(30000)
                    .status(PaymentStatus.DONE)
                    .user(user)
                    .reservation(validReservation)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            Long wrongReservationId = 999L;

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.getPayment(authUser, wrongReservationId, paymentId)
            );

            assertEquals(ErrorCode.PAYMENT_RESERVATION_MISMATCH.getDefaultMessage(), exception.getMessage());
        }


        @Test
        void 결제_조회_완료() {
            // given
            Reservation reservation = Reservation.builder()
                    .id(reservationId)
                    .user(user)
                    .store(store)
                    .reservedAt(LocalDateTime.of(2025, 4, 13, 12, 0))
                    .build();

            Payment payment = Payment.builder()
                    .id(paymentId)
                    .paymentKey("tgen_20250410150357N34V7")
                    .method("CARD")
                    .price(30000)
                    .status(PaymentStatus.DONE)
                    .user(user)
                    .reservation(reservation)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            // when
            PaymentResponseDto response = paymentService.getPayment(authUser, reservationId, paymentId);

            // then
            assertEquals(paymentId, response.getPaymentId());
            assertEquals(reservationId, response.getReservationId());
            assertEquals(authUser.getId(), response.getUserId());
            assertEquals("CARD", response.getMethod());
            assertEquals(30000, response.getPrice());
            assertEquals(PaymentStatus.DONE, response.getStatus());
        }
    }

    @Nested
    class 결제_취소 {

        @Test
        void 결제_내역이_없는_경우_예외_발생() {
            // given
            given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.cancelPayment(authUser, reservationId, paymentId)
            );

            assertEquals(ErrorCode.PAYMENT_NOT_FOUND.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 다른_유저가_내_결제를_취소하려는_경우_예외_발생() {
            // given
            AuthUser otherAuthUser = new AuthUser(99L, "other@a.com", UserRole.ROLE_USER, "다른회원");

            Reservation myReservation = Reservation.builder()
                    .id(reservationId)
                    .user(user)
                    .store(store)
                    .reservedAt(LocalDateTime.of(2025, 4, 13, 12, 0))
                    .build();

            Payment myPayment = Payment.builder()
                    .id(paymentId)
                    .paymentKey("tgen_20250410150357N34V7")
                    .method("CARD")
                    .price(30000)
                    .status(PaymentStatus.DONE)
                    .user(user)
                    .reservation(myReservation)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(myPayment));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.cancelPayment(otherAuthUser, reservationId, paymentId)
            );

            assertEquals(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 결제_내역의_예약ID와_입력받은_예약ID가_다른_경우_예외_발생() {
            // given
            Reservation validReservation = Reservation.builder()
                    .id(reservationId)
                    .user(user)
                    .store(store)
                    .reservedAt(LocalDateTime.of(2025, 4, 13, 12, 0))
                    .build();

            Payment payment = Payment.builder()
                    .id(paymentId)
                    .paymentKey("tgen_20250410150357N34V7")
                    .method("CARD")
                    .price(30000)
                    .status(PaymentStatus.DONE)
                    .user(user)
                    .reservation(validReservation)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            Long wrongReservationId = 999L;

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.cancelPayment(authUser, wrongReservationId, paymentId)
            );

            assertEquals(ErrorCode.PAYMENT_RESERVATION_MISMATCH.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이미_취소된_결제인_경우_예외_발생() {
            // given
            Reservation validReservation = Reservation.builder()
                    .id(reservationId)
                    .user(user)
                    .store(store)
                    .reservedAt(LocalDateTime.of(2025, 4, 13, 12, 0))
                    .build();

            Payment canceledPayment = Payment.builder()
                    .id(paymentId)
                    .paymentKey("tgen_20250410150357N34V7")
                    .method("CARD")
                    .price(30000)
                    .status(PaymentStatus.CANCELED)
                    .user(user)
                    .reservation(validReservation)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(canceledPayment));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.cancelPayment(authUser, reservationId, paymentId)
            );

            assertEquals(ErrorCode.ALREADY_CANCELED.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void tosspayment에서_결제_취소가_실패한_경우_예외_발생() {
            // given
            Reservation validReservation = Reservation.builder()
                    .id(reservationId)
                    .user(user)
                    .store(store)
                    .reservedAt(LocalDateTime.of(2025, 4, 13, 12, 0))
                    .build();

            Payment payment = Payment.builder()
                    .id(paymentId)
                    .paymentKey("tgen_20250410150357N34V7")
                    .method("CARD")
                    .price(30000)
                    .status(PaymentStatus.DONE)
                    .user(user)
                    .reservation(validReservation)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            TossPaymentCancelResponseDto failedCancelResponse = TossPaymentCancelResponseDto.builder()
                    .paymentKey("tgen_20250410150357N34V7")
                    .orderId("reservation-" + reservationId)
                    .status("FAILED")
                    .canceledAt("2025-04-11T12:00:00Z")
                    .build();

            given(tossPaymentClient.cancelPayment(payment.getPaymentKey(), "고객 요청으로 결제 취소"))
                    .willReturn(failedCancelResponse);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    paymentService.cancelPayment(authUser, reservationId, paymentId)
            );

            assertEquals(ErrorCode.TOSS_PAYMENT_CANCEL_FAILED.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 결제_취소_완료() {
            // given
            Reservation validReservation = Reservation.builder()
                    .id(reservationId)
                    .user(user)
                    .store(store)
                    .reservedAt(LocalDateTime.of(2025, 4, 13, 12, 0))
                    .build();

            Payment payment = Payment.builder()
                    .id(paymentId)
                    .paymentKey("tgen_20250410150357N34V7")
                    .method("CARD")
                    .price(30000)
                    .status(PaymentStatus.DONE)
                    .user(user)
                    .reservation(validReservation)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            TossPaymentCancelResponseDto successResponse = TossPaymentCancelResponseDto.builder()
                    .paymentKey("tgen_20250410150357N34V7")
                    .orderId("reservation-" + reservationId)
                    .status("CANCELED")
                    .canceledAt("2025-04-11T12:00:00Z")
                    .build();

            given(tossPaymentClient.cancelPayment(payment.getPaymentKey(), "고객 요청으로 결제 취소"))
                    .willReturn(successResponse);

            given(paymentRepository.saveAndFlush(any(Payment.class)))
                    .willAnswer(invocation -> {
                        Payment saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", paymentId);
                        return saved;
                    });

            // when
            PaymentResponseDto response = paymentService.cancelPayment(authUser, reservationId, paymentId);

            // then
            assertEquals(paymentId, response.getPaymentId());
            assertEquals(reservationId, response.getReservationId());
            assertEquals(authUser.getId(), response.getUserId());
            assertEquals("CARD", response.getMethod());
            assertEquals(30000, response.getPrice());
            assertEquals(PaymentStatus.CANCELED, response.getStatus());
        }
    }
}
