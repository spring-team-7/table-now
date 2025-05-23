package org.example.tablenow.domain.settlement.service;

import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.settlement.dto.response.SettlementOperationResponseDto;
import org.example.tablenow.domain.settlement.dto.response.SettlementResponseDto;
import org.example.tablenow.domain.settlement.dto.response.SettlementSummaryPageDto;
import org.example.tablenow.domain.settlement.entity.Settlement;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;
import org.example.tablenow.domain.settlement.repository.SettlementRepository;
import org.example.tablenow.domain.store.entity.Store;
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
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private SettlementService settlementService;

    Long userId = 1L;
    Long ownerId = 2L;
    AuthUser authUser = new AuthUser(userId, "user@a.com", UserRole.ROLE_USER, "고객");
    AuthUser authOwner = new AuthUser(ownerId, "owner@a.com", UserRole.ROLE_OWNER, "사장님");
    User user = User.fromAuthUser(authUser);
    User owner = User.fromAuthUser(authOwner);

    Long paymentId = 1L;
    Long settlementId = 1L;
    Payment payment;
    Settlement settlement;

    @BeforeEach
    void setUp() {

        Store store = Store.builder()
                .id(10L)
                .name("맛집")
                .user(owner)
                .build();

        Reservation reservation = Reservation.builder()
                .id(100L)
                .user(user)
                .store(store)
                .reservedAt(LocalDateTime.now().plusDays(1))
                .build();

        payment = Payment.builder()
                .id(paymentId)
                .price(10000)
                .status(PaymentStatus.DONE)
                .reservation(reservation)
                .build();

        settlement = Settlement.builder()
                .id(settlementId)
                .payment(payment)
                .amount(10000)
                .status(SettlementStatus.DONE)
                .build();
    }

    @Nested
    class 내_매장_정산_조회 {

        @Test
        void 로그인_유저의_매장_정산_조회_성공() {

            // given
            Settlement readySettlement = Settlement.builder()
                    .id(2L)
                    .payment(payment)
                    .amount(5000)
                    .status(SettlementStatus.READY)
                    .build();

            Settlement canceledSettlement = Settlement.builder()
                    .id(3L)
                    .payment(payment)
                    .amount(3000)
                    .status(SettlementStatus.CANCELED)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Settlement> page = new PageImpl<>(List.of(settlement, readySettlement, canceledSettlement), pageable, 3);
            given(settlementRepository.findByStoreOwnerId(ownerId, pageable)).willReturn(page);

            // when
            SettlementSummaryPageDto summary = settlementService.getMyStoreSettlements(authOwner, 1, 10);

            // then
            assertAll(
                    () -> assertThat(summary.getDoneAmount()).isEqualTo(10000),
                    () -> assertThat(summary.getReadyAmount()).isEqualTo(5000),
                    () -> assertThat(summary.getCanceledAmount()).isEqualTo(3000),
                    () -> assertThat(summary.getPage().getTotalElements()).isEqualTo(3)
            );
        }
    }

    @Nested
    class 단건_조회 {

        @Test
        void 없는_정산ID_조회시_예외_발생() {

            // given
            given(settlementRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    settlementService.getSettlement(settlementId));

            // then
            assertEquals(ErrorCode.SETTLEMENT_NOT_FOUND.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 정산_단건_조회_성공() {

            // given
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            SettlementResponseDto response = settlementService.getSettlement(settlementId);

            // then
            assertAll(
                    () -> assertThat(response.getId()).isEqualTo(settlementId),
                    () -> assertThat(response.getAmount()).isEqualTo(settlement.getAmount()),
                    () -> assertThat(response.getStatus()).isEqualTo(settlement.getStatus().name())
            );
        }
    }

    @Nested
    class 전체_조회 {

        @Test
        void 정산_페이징_조회_성공() {

            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Settlement> page = new PageImpl<>(List.of(settlement), pageable, 1);
            given(settlementRepository.findAll(any(Pageable.class))).willReturn(page);

            // when
            Page<SettlementResponseDto> result = settlementService.getAllSettlements(1, 10);

            // then
            assertAll(
                    () -> assertThat(result.getTotalElements()).isEqualTo(1),
                    () -> assertThat(result.getContent().get(0).getId()).isEqualTo(settlement.getId())
            );
        }
    }

    @Nested
    class 정산_취소 {

        @Test
        void 정산_ID_없을_경우_예외_발생() {

            // given
            given(settlementRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    settlementService.cancelSettlement(settlementId));

            // then
            assertEquals(ErrorCode.SETTLEMENT_NOT_FOUND.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 정산_취소_성공() {

            // given
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            SettlementOperationResponseDto result = settlementService.cancelSettlement(settlementId);

            // then
            assertAll(
                    () -> assertThat(result.getCount()).isEqualTo(1),
                    () -> assertThat(result.getStatus()).isEqualTo(SettlementStatus.CANCELED)
            );
        }
    }
}
