package org.example.tablenow.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.dto.request.PaymentCreateRequestDto;
import org.example.tablenow.domain.payment.dto.response.PaymentCancelResponseDto;
import org.example.tablenow.domain.payment.dto.response.PaymentCreateResponseDto;
import org.example.tablenow.domain.payment.dto.response.PaymentGetResponseDto;
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

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

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

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "User not found"));

        // 결제 생성
        Payment payment = new Payment(
                "Init",
                paymentCreateRequestDto.getMethod(),
                paymentCreateRequestDto.getPrice(),
                PaymentStatus.READY, // 초기 상태 READY
                user,
                reservation
        );

        // tosspayment logic


        // 결제 상태 DONE으로 변경 후 저장
        payment.changePaymentStatus(PaymentStatus.DONE);
        paymentRepository.save(payment);

        return PaymentCreateResponseDto.from(payment);
    }

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

        return PaymentGetResponseDto.from(payment);
    }

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

        // 결제 상태 CANCELED로 변경 후 저장
        payment.changePaymentStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);

        return PaymentCancelResponseDto.from(payment);
    }

    private boolean verify(AuthUser authUser, Reservation reservation) {

//        // 본인 예약에 대한 결제, 조회, 취소인지 확인
//        if (!Objects.equals(authUser.getId(), reservation.getUser().getId())) {
//            throw new HandledException(ErrorCode.AUTHORIZATION, "Unauthorized");
//        } // Reservation 필요(reservation.getUser().getId())

        // ROLE_USER 권한 확인
        boolean hasUserRole = authUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(UserRole.ROLE_USER.name()));

        if (!hasUserRole) {
            throw new HandledException(ErrorCode.AUTHORIZATION, "Unauthorized");
        }

        return true;
    }
}
