package org.example.tablenow.domain.payment.service;

import org.example.tablenow.domain.category.entity.Category;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
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

        @Test
        void 로그인_한_유저와_예약한_유저가_다른_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void 이미_결제된_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void Client에서_전달받은_금액과_가게의_예약금이_다른_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void tosspayment에서_결제가_완료되지_않은_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void 결제_완료() {
            // given

            // when

            // then

        }
    }

    @Nested
    class 결제_조회 {

        @Test
        void 결제_내역이_없는_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void 로그인_한_유저와_예약한_유저가_다른_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void 결제_내역의_예약ID와_입력받은_예약ID가_다른_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void 결제_조회_완료() {
            // given

            // when

            // then

        }
    }

    @Nested
    class 결제_취소 {

        @Test
        void 결제_내역이_없는_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void 로그인_한_유저와_예약한_유저가_다른_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void 결제_내역의_예약ID와_입력받은_예약ID가_다른_경우_예외_발생() {
            // given

            // when

            // then

        }
        @Test
        void 이미_취소된_결제인_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void tosspayment에서_결제_취소가_실패한_경우_예외_발생() {
            // given

            // when

            // then

        }

        @Test
        void 결제_취소_완료() {
            // given

            // when

            // then

        }
    }
}
