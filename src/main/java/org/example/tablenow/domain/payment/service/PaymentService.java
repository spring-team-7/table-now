package org.example.tablenow.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.dto.request.PaymentCreateRequestDto;
import org.example.tablenow.domain.payment.dto.response.*;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;
import org.example.tablenow.domain.payment.repository.PaymentRepository;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.user.entity.User;
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
    private final TossPaymentClient tossPaymentClient;

    private final ReservationService reservationService;

    @Transactional
    public PaymentResponseDto confirmPayment(AuthUser authUser, Long reservationId, PaymentCreateRequestDto paymentCreateRequestDto) {

        Reservation reservation = reservationService.getReservation(reservationId);

        if (!Objects.equals(authUser.getId(), reservation.getUser().getId())) {
            throw new HandledException(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
        }

        if (paymentRepository.existsByReservation_Id(reservationId)) {
            throw new HandledException(ErrorCode.ALREADY_PAID);
        }

        // 결제 금액 위변조 방지
        if (paymentCreateRequestDto.getAmount() != reservation.getStore().getDeposit()) {
            throw new HandledException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        User user = User.fromAuthUser(authUser);

        Payment payment = Payment.builder()
                .paymentKey(paymentCreateRequestDto.getPaymentKey())
                .method("Init")
                .price(paymentCreateRequestDto.getAmount())
                .status(PaymentStatus.READY) // 초기 상태 READY
                .user(user)
                .reservation(reservation)
                .build();

        // tosspayment 결제 승인
        TossPaymentConfirmResponseDto tossResponse = tossPaymentClient.confirmPayment(paymentCreateRequestDto);

        PaymentStatus status = PaymentStatus.from(tossResponse.getStatus(), ErrorCode.TOSS_PAYMENT_FAILED);

        if (status != PaymentStatus.DONE) {
            throw new HandledException(ErrorCode.TOSS_PAYMENT_FAILED);
        }

        payment.changePaymentMethod(tossResponse.getMethod());
        payment.changePaymentStatus(PaymentStatus.valueOf(tossResponse.getStatus()));
        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponseDto.fromPayment(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(AuthUser authUser, Long reservationId, Long paymentId) {

        Payment payment = getVerifiedPayment(authUser, reservationId, paymentId);

        return PaymentResponseDto.fromPayment(payment);
    }

    @Transactional
    public PaymentResponseDto cancelPayment(AuthUser authUser, Long reservationId, Long paymentId) {

        Payment payment = getVerifiedPayment(authUser, reservationId, paymentId);

        if (payment.isCanceled()) {
            throw new HandledException(ErrorCode.ALREADY_CANCELED);
        }

        // tosspayment 결제 취소
        TossPaymentCancelResponseDto tossResponse = tossPaymentClient.cancelPayment(
                payment.getPaymentKey(), "고객 요청으로 결제 취소"
        );

        PaymentStatus status = PaymentStatus.from(tossResponse.getStatus(), ErrorCode.TOSS_PAYMENT_CANCEL_FAILED);

        if (status != PaymentStatus.CANCELED) {
            throw new HandledException(ErrorCode.TOSS_PAYMENT_CANCEL_FAILED);
        }

        payment.changePaymentStatus(PaymentStatus.CANCELED);
        Payment savedPayment = paymentRepository.saveAndFlush(payment);

        return PaymentResponseDto.fromPayment(savedPayment);
    }

    public Payment getPayment(Long paymentId) {

        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new HandledException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private Payment getVerifiedPayment(AuthUser authUser, Long reservationId, Long paymentId) {

        Payment payment = getPayment(paymentId);

        if (!Objects.equals(authUser.getId(), payment.getReservation().getUser().getId())) {
            throw new HandledException(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
        }

        if (!Objects.equals(reservationId, payment.getReservation().getId())) {
            throw new HandledException(ErrorCode.PAYMENT_RESERVATION_MISMATCH);
        }

        return payment;
    }
}
