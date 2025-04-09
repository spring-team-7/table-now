package org.example.tablenow.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.dto.request.PaymentCreateRequestDto;
import org.example.tablenow.domain.payment.dto.response.*;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;
import org.example.tablenow.domain.payment.repository.PaymentRepository;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.repository.ReservationRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public PaymentCreateResponseDto confirmPayment(AuthUser authUser, Long reservationId, PaymentCreateRequestDto paymentCreateRequestDto) {

        // 예약 확인
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "Reservation not found"));

        // 본인의 예약에 대한 결제, 조회, 취소 확인 / 유저 권한을 가지고 있는지 확인
        verify(authUser, reservation);

        // 이미 결제된 예약인지 확인
        if (paymentRepository.existsByReservationId(reservationId)) {
            throw new HandledException(ErrorCode.BAD_REQUEST, "Reservation already paid.");
        }

        // 결제 금액 위변조 방지
        if (paymentCreateRequestDto.getAmount() != reservation.getStore().getDeposit()) {
            throw new HandledException(ErrorCode.BAD_REQUEST, "결제 금액이 예약 보증금과 일치하지 않습니다.");
        }

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "User not found"));

        // 결제 생성
        Payment payment = new Payment(
                paymentCreateRequestDto.getPaymentKey(),
                "Init",
                paymentCreateRequestDto.getAmount(),
                PaymentStatus.READY, // 초기 상태 READY
                user,
                reservation
        );

        // tosspayment 승인
        TossPaymentConfirmResponseDto tossResponse = tossPaymentClient.confirmPayment(paymentCreateRequestDto);

        if (!"DONE".equalsIgnoreCase(tossResponse.getStatus())) {
            throw new HandledException(ErrorCode.BAD_REQUEST, "Toss 결제 실패");
        }

        // tosspayment에서 받아온 method로 저장
        payment.changePaymentMethod(tossResponse.getMethod());

        // 결제 상태 DONE으로 변경 후 저장
        payment.changePaymentStatus(PaymentStatus.valueOf(tossResponse.getStatus()));
        paymentRepository.save(payment);

        return PaymentCreateResponseDto.fromPayment(payment);
    }

    @Transactional(readOnly = true)
    public PaymentGetResponseDto getPayment(AuthUser authUser, Long reservationId, Long paymentId) {

        // 예약 확인
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "Reservation not found"));

        // 본인의 예약에 대한 결제, 조회, 취소 확인, 유저 권한을 가지고 있는지 확인
        verify(authUser, reservation);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "Payment not found"));

        // 결제 객체의 ReservationId와 입력 받은 ReservationId 비교
        if (!payment.getReservation().getId().equals(reservationId)) {
            throw new HandledException(ErrorCode.BAD_REQUEST, "Reservation ID does not match with Payment");
        }

        return PaymentGetResponseDto.fromPayment(payment);
    }

    @Transactional
    public PaymentCancelResponseDto cancelPayment(AuthUser authUser, Long reservationId, Long paymentId) {

        // 예약 확인
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "Reservation not found"));

        // 본인의 예약에 대한 결제, 조회, 취소 확인, 유저 권한을 가지고 있는지 확인
        verify(authUser, reservation);

        // 결제 Id 확인
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "Payment not found"));

        // 예약 Id 일치하는지 확인
        if (!payment.getReservation().getId().equals(reservationId)) {
            throw new HandledException(ErrorCode.BAD_REQUEST, "Reservation ID does not match with Payment");
        }

        // 이미 취소된 결제인지 확인
        if (payment.getStatus() == PaymentStatus.CANCELED) {
            throw new HandledException(ErrorCode.BAD_REQUEST, "Payment already canceled");
        }

        // Toss 결제 취소 요청
        TossPaymentCancelResponseDto tossResponse = tossPaymentClient.cancelPayment(
                payment.getPaymentKey(), "고객 요청으로 결제 취소"
        );

        if (!"CANCELED".equalsIgnoreCase(tossResponse.getStatus())) {
            throw new HandledException(ErrorCode.BAD_REQUEST, "Toss 결제 취소 실패");
        }

        // 결제 상태 CANCELED로 변경 후 저장
        payment.changePaymentStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);

        return PaymentCancelResponseDto.fromPayment(payment);
    }

    private boolean verify(AuthUser authUser, Reservation reservation) {

        // 본인 예약에 대한 결제, 조회, 취소인지 확인
        if (!Objects.equals(authUser.getId(), reservation.getUser().getId())) {
            throw new HandledException(ErrorCode.AUTHORIZATION, "Unauthorized");
        }

        // ROLE_USER 권한 확인
        boolean hasUserRole = authUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(UserRole.ROLE_USER.name()));

        if (!hasUserRole) {
            throw new HandledException(ErrorCode.AUTHORIZATION, "Unauthorized");
        }

        return true;
    }
}
